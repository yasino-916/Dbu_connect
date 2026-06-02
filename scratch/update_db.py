import psycopg2

def main():
    try:
        conn = psycopg2.connect(
            host="aws-0-eu-west-1.pooler.supabase.com",
            port=6543,
            database="postgres",
            user="postgres.hyxknslobfjabpwkcztk",
            password="GecH    123e4r"
        )
        conn.autocommit = True
        with conn.cursor() as cur:
            print("Adding typing_user_id column if not exists...")
            cur.execute("ALTER TABLE public.matches ADD COLUMN IF NOT EXISTS typing_user_id uuid REFERENCES public.profiles(id) ON DELETE SET NULL;")
            
            print("Dropping old function versions...")
            cur.execute("DROP FUNCTION IF EXISTS public.like_profile(uuid, boolean);")
            cur.execute("DROP FUNCTION IF EXISTS public.get_matches_for_current_user();")
            
            print("Recreating public.get_matches_for_current_user()...")
            get_matches_sql = """
create or replace function public.get_matches_for_current_user()
returns table (
    id uuid,
    user_a_id uuid,
    user_b_id uuid,
    user_name text,
    user_photo_url text,
    created_at bigint,
    is_new boolean,
    last_message text,
    last_message_time bigint,
    unread_count integer,
    is_online boolean,
    is_typing boolean
)
language plpgsql
security definer
set search_path = public
as $$
declare
    current_user_id uuid := auth.uid();
begin
    if current_user_id is null then
        raise exception 'Not authenticated';
    end if;

    return query
    select
        m.id,
        m.user_a_id,
        m.user_b_id,
        other_profile.name as user_name,
        coalesce(other_profile.photos[1], '') as user_photo_url,
        m.created_at,
        m.is_new,
        m.last_message,
        m.last_message_time,
        (select count(*)::integer from public.messages msg where msg.chat_id = m.id and msg.sender_id <> current_user_id and msg.status <> 'READ') as unread_count,
        m.is_online,
        coalesce(m.typing_user_id = other_profile.id, false) as is_typing
    from public.matches m
    join public.profiles other_profile
      on other_profile.id = case
          when m.user_a_id = current_user_id then m.user_b_id
          else m.user_a_id
      end
    where (m.user_a_id = current_user_id or m.user_b_id = current_user_id)
      and not public.users_are_blocked(current_user_id, other_profile.id)
    order by coalesce(m.last_message_time, m.created_at) desc;
end;
$$;
            """
            cur.execute(get_matches_sql)
            cur.execute("grant execute on function public.get_matches_for_current_user() to authenticated;")
            
            print("Recreating public.like_profile()...")
            like_profile_sql = """
create or replace function public.like_profile(target_user_id uuid, is_super_like boolean default false)
returns table (
    id uuid,
    user_a_id uuid,
    user_b_id uuid,
    user_name text,
    user_photo_url text,
    created_at bigint,
    is_new boolean,
    last_message text,
    last_message_time bigint,
    unread_count integer,
    is_online boolean,
    is_typing boolean
)
language plpgsql
security definer
set search_path = public
as $$
declare
    current_user_id uuid := auth.uid();
    matched_id uuid;
begin
    if current_user_id is null then
        raise exception 'Not authenticated';
    end if;

    if target_user_id = current_user_id then
        raise exception 'You cannot like your own profile';
    end if;

    if public.users_are_blocked(current_user_id, target_user_id) then
        return;
    end if;

    if not exists (
        select 1
        from public.profiles p
        where p.id = target_user_id
    ) then
        raise exception 'Profile not found';
    end if;

    insert into public.profile_actions(actor_id, target_id, action, is_super_like)
    values (current_user_id, target_user_id, 'like', is_super_like)
    on conflict (actor_id, target_id)
    do update set action = 'like', is_super_like = excluded.is_super_like, created_at = now();

    if exists (
        select 1
        from public.profile_actions
        where actor_id = target_user_id
          and target_id = current_user_id
          and action = 'like'
    ) then
        insert into public.matches(user_a_id, user_b_id, created_at, is_new)
        values (
            least(current_user_id, target_user_id),
            greatest(current_user_id, target_user_id),
            (extract(epoch from now()) * 1000)::bigint,
            true
        )
        on conflict do nothing;

        select m.id into matched_id
        from public.matches m
        where least(m.user_a_id, m.user_b_id) = least(current_user_id, target_user_id)
          and greatest(m.user_a_id, m.user_b_id) = greatest(current_user_id, target_user_id)
        limit 1;

        return query
        select
            existing_match.id,
            existing_match.user_a_id,
            existing_match.user_b_id,
            existing_match.user_name,
            existing_match.user_photo_url,
            existing_match.created_at,
            existing_match.is_new,
            existing_match.last_message,
            existing_match.last_message_time,
            existing_match.unread_count,
            existing_match.is_online,
            existing_match.is_typing
        from public.get_matches_for_current_user() existing_match
        where existing_match.id = matched_id;
    end if;

    return;
end;
$$;
            """
            cur.execute(like_profile_sql)
            cur.execute("grant execute on function public.like_profile(uuid, boolean) to authenticated;")
            
            print("Database functions successfully updated with typing status and unread messages count features!")
            
    except Exception as e:
        print(f"Error executing database update: {e}")
    finally:
        if 'conn' in locals() and conn:
            conn.close()

if __name__ == "__main__":
    main()
