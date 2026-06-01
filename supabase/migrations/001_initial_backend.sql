-- DBU Connect Supabase backend schema, RLS policies, and RPC functions.
-- Apply this in the Supabase SQL editor or with the Supabase CLI/psql from a secure shell.
-- Do not put the database connection string in the Android app.

create extension if not exists pgcrypto;

-- ─────────────────────────────────────────────────────────────────────────────
-- Core tables
-- ─────────────────────────────────────────────────────────────────────────────

create table if not exists public.profiles (
    id uuid primary key references auth.users(id) on delete cascade,
    name text not null default '',
    age integer not null default 18 check (age between 16 and 100),
    department text not null default '',
    year integer not null default 1 check (year between 1 and 8),
    bio text not null default '',
    photos text[] not null default '{}',
    interests text[] not null default '{}',
    intent text not null default 'Friends',
    email text not null default '',
    phone text not null default '',
    is_profile_complete boolean not null default false,
    is_admin boolean not null default false,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table if not exists public.privacy_settings (
    user_id uuid primary key references public.profiles(id) on delete cascade,
    show_department boolean not null default true,
    show_year boolean not null default true,
    hide_profile boolean not null default false,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table if not exists public.profile_actions (
    id uuid primary key default gen_random_uuid(),
    actor_id uuid not null references public.profiles(id) on delete cascade,
    target_id uuid not null references public.profiles(id) on delete cascade,
    action text not null check (action in ('like', 'pass')),
    created_at timestamptz not null default now(),
    constraint profile_actions_not_self check (actor_id <> target_id),
    constraint profile_actions_actor_target_unique unique (actor_id, target_id)
);

create table if not exists public.matches (
    id uuid primary key default gen_random_uuid(),
    user_a_id uuid not null references public.profiles(id) on delete cascade,
    user_b_id uuid not null references public.profiles(id) on delete cascade,
    user_name text not null default '',
    user_photo_url text not null default '',
    created_at bigint not null default ((extract(epoch from now()) * 1000)::bigint),
    is_new boolean not null default true,
    last_message text,
    last_message_time bigint,
    unread_count integer not null default 0,
    is_online boolean not null default false,
    constraint matches_not_self check (user_a_id <> user_b_id)
);

create unique index if not exists matches_users_unique
    on public.matches (least(user_a_id, user_b_id), greatest(user_a_id, user_b_id));

create table if not exists public.messages (
    id uuid primary key default gen_random_uuid(),
    chat_id uuid not null references public.matches(id) on delete cascade,
    sender_id uuid not null references public.profiles(id) on delete cascade,
    text text not null check (length(trim(text)) > 0),
    timestamp bigint not null default ((extract(epoch from now()) * 1000)::bigint),
    status text not null default 'SENT' check (status in ('SENDING', 'SENT', 'DELIVERED', 'READ', 'FAILED')),
    created_at timestamptz not null default now()
);

create table if not exists public.events (
    id uuid primary key default gen_random_uuid(),
    title text not null check (length(trim(title)) > 0),
    description text not null default '',
    date_time bigint not null,
    end_time bigint,
    location text not null default '',
    image_url text not null default '',
    tags text[] not null default '{}',
    rsvp_status text not null default 'NONE',
    attendee_count integer not null default 0,
    attendees_from_dept integer not null default 0,
    created_by uuid references public.profiles(id) on delete set null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table if not exists public.event_rsvps (
    event_id uuid not null references public.events(id) on delete cascade,
    user_id uuid not null references public.profiles(id) on delete cascade,
    status text not null check (status in ('NONE', 'GOING', 'INTERESTED', 'NOT_GOING')),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    primary key (event_id, user_id)
);

create table if not exists public.blocks (
    blocker_id uuid not null references public.profiles(id) on delete cascade,
    blocked_id uuid not null references public.profiles(id) on delete cascade,
    created_at timestamptz not null default now(),
    primary key (blocker_id, blocked_id),
    constraint blocks_not_self check (blocker_id <> blocked_id)
);

create table if not exists public.reports (
    id uuid primary key default gen_random_uuid(),
    reporter_id uuid not null references public.profiles(id) on delete cascade,
    reported_id uuid not null references public.profiles(id) on delete cascade,
    reason text not null default 'Inappropriate behavior',
    details text not null default '',
    created_at timestamptz not null default now(),
    status text not null default 'OPEN' check (status in ('OPEN', 'REVIEWED', 'DISMISSED', 'ACTIONED')),
    constraint reports_not_self check (reporter_id <> reported_id)
);

create index if not exists profile_actions_actor_idx on public.profile_actions(actor_id);
create index if not exists profile_actions_target_idx on public.profile_actions(target_id);
create index if not exists matches_user_a_idx on public.matches(user_a_id);
create index if not exists matches_user_b_idx on public.matches(user_b_id);
create index if not exists messages_chat_timestamp_idx on public.messages(chat_id, timestamp);
create index if not exists event_rsvps_user_idx on public.event_rsvps(user_id);
create index if not exists reports_reported_idx on public.reports(reported_id);

-- ─────────────────────────────────────────────────────────────────────────────
-- Timestamps and message denormalization
-- ─────────────────────────────────────────────────────────────────────────────

create or replace function public.set_updated_at()
returns trigger
language plpgsql
as $$
begin
    new.updated_at = now();
    return new;
end;
$$;

drop trigger if exists profiles_set_updated_at on public.profiles;
create trigger profiles_set_updated_at
before update on public.profiles
for each row execute function public.set_updated_at();

drop trigger if exists privacy_settings_set_updated_at on public.privacy_settings;
create trigger privacy_settings_set_updated_at
before update on public.privacy_settings
for each row execute function public.set_updated_at();

drop trigger if exists events_set_updated_at on public.events;
create trigger events_set_updated_at
before update on public.events
for each row execute function public.set_updated_at();

drop trigger if exists event_rsvps_set_updated_at on public.event_rsvps;
create trigger event_rsvps_set_updated_at
before update on public.event_rsvps
for each row execute function public.set_updated_at();

create or replace function public.update_match_last_message()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
    update public.matches
    set last_message = new.text,
        last_message_time = new.timestamp
    where id = new.chat_id;

    return new;
end;
$$;

drop trigger if exists messages_update_match_last_message on public.messages;
create trigger messages_update_match_last_message
after insert on public.messages
for each row execute function public.update_match_last_message();

-- ─────────────────────────────────────────────────────────────────────────────
-- Helper functions
-- ─────────────────────────────────────────────────────────────────────────────

create or replace function public.current_user_is_admin()
returns boolean
language sql
stable
security definer
set search_path = public
as $$
    select exists (
        select 1
        from public.profiles
        where id = auth.uid()
          and is_admin = true
    );
$$;

create or replace function public.users_are_blocked(left_user_id uuid, right_user_id uuid)
returns boolean
language sql
stable
security definer
set search_path = public
as $$
    select exists (
        select 1
        from public.blocks
        where (blocker_id = left_user_id and blocked_id = right_user_id)
           or (blocker_id = right_user_id and blocked_id = left_user_id)
    );
$$;

-- ─────────────────────────────────────────────────────────────────────────────
-- RPC functions used by the Android app
-- ─────────────────────────────────────────────────────────────────────────────

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
    is_online boolean
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
        m.unread_count,
        m.is_online
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

create or replace function public.like_profile(target_user_id uuid)
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
    is_online boolean
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
        from public.profiles
        where id = target_user_id
          and is_profile_complete = true
    ) then
        raise exception 'Profile not found';
    end if;

    insert into public.profile_actions(actor_id, target_id, action)
    values (current_user_id, target_user_id, 'like')
    on conflict (actor_id, target_id)
    do update set action = 'like', created_at = now();

    if exists (
        select 1
        from public.profile_actions
        where actor_id = target_user_id
          and target_id = current_user_id
          and action = 'like'
    ) then
        insert into public.matches(user_a_id, user_b_id, created_at, is_new)
        values (current_user_id, target_user_id, (extract(epoch from now()) * 1000)::bigint, true)
        on conflict do nothing;

        select m.id into matched_id
        from public.matches m
        where least(m.user_a_id, m.user_b_id) = least(current_user_id, target_user_id)
          and greatest(m.user_a_id, m.user_b_id) = greatest(current_user_id, target_user_id)
        limit 1;

        return query
        select *
        from public.get_matches_for_current_user() existing_match
        where existing_match.id = matched_id;
    end if;

    return;
end;
$$;

create or replace function public.pass_profile(target_user_id uuid)
returns void
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

    if target_user_id = current_user_id then
        raise exception 'You cannot pass your own profile';
    end if;

    insert into public.profile_actions(actor_id, target_id, action)
    values (current_user_id, target_user_id, 'pass')
    on conflict (actor_id, target_id)
    do update set action = 'pass', created_at = now();
end;
$$;

create or replace function public.update_privacy_settings(
    show_department boolean,
    show_year boolean,
    hide_profile boolean
)
returns void
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

    insert into public.privacy_settings(user_id, show_department, show_year, hide_profile)
    values (current_user_id, show_department, show_year, hide_profile)
    on conflict (user_id)
    do update set
        show_department = excluded.show_department,
        show_year = excluded.show_year,
        hide_profile = excluded.hide_profile,
        updated_at = now();
end;
$$;

create or replace function public.get_events_for_current_user()
returns table (
    id uuid,
    title text,
    description text,
    date_time bigint,
    end_time bigint,
    location text,
    image_url text,
    tags text[],
    rsvp_status text,
    attendee_count integer,
    attendees_from_dept integer
)
language plpgsql
security definer
set search_path = public
as $$
declare
    current_user_id uuid := auth.uid();
    current_department text;
begin
    if current_user_id is null then
        raise exception 'Not authenticated';
    end if;

    select p.department into current_department
    from public.profiles p
    where p.id = current_user_id;

    return query
    select
        e.id,
        e.title,
        e.description,
        e.date_time,
        e.end_time,
        e.location,
        e.image_url,
        e.tags,
        coalesce(my_rsvp.status, 'NONE') as rsvp_status,
        count(going_rsvp.user_id)::integer as attendee_count,
        count(attendee_profile.id) filter (
            where attendee_profile.department = current_department
              and current_department is not null
              and current_department <> ''
        )::integer as attendees_from_dept
    from public.events e
    left join public.event_rsvps my_rsvp
      on my_rsvp.event_id = e.id
     and my_rsvp.user_id = current_user_id
    left join public.event_rsvps going_rsvp
      on going_rsvp.event_id = e.id
     and going_rsvp.status = 'GOING'
    left join public.profiles attendee_profile
      on attendee_profile.id = going_rsvp.user_id
    group by e.id, my_rsvp.status
    order by e.date_time asc;
end;
$$;

create or replace function public.rsvp_event(target_event_id uuid, new_status text)
returns table (
    id uuid,
    title text,
    description text,
    date_time bigint,
    end_time bigint,
    location text,
    image_url text,
    tags text[],
    rsvp_status text,
    attendee_count integer,
    attendees_from_dept integer
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

    if new_status not in ('NONE', 'GOING', 'INTERESTED', 'NOT_GOING') then
        raise exception 'Invalid RSVP status';
    end if;

    if not exists (select 1 from public.events where events.id = target_event_id) then
        raise exception 'Event not found';
    end if;

    insert into public.event_rsvps(event_id, user_id, status)
    values (target_event_id, current_user_id, new_status)
    on conflict (event_id, user_id)
    do update set status = excluded.status, updated_at = now();

    update public.events e
    set attendee_count = (
        select count(*)::integer
        from public.event_rsvps r
        where r.event_id = target_event_id
          and r.status = 'GOING'
    )
    where e.id = target_event_id;

    return query
    select *
    from public.get_events_for_current_user() current_event
    where current_event.id = target_event_id;
end;
$$;

create or replace function public.block_user(target_user_id uuid)
returns void
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

    if target_user_id = current_user_id then
        raise exception 'You cannot block yourself';
    end if;

    insert into public.blocks(blocker_id, blocked_id)
    values (current_user_id, target_user_id)
    on conflict do nothing;

    delete from public.matches m
    where least(m.user_a_id, m.user_b_id) = least(current_user_id, target_user_id)
      and greatest(m.user_a_id, m.user_b_id) = greatest(current_user_id, target_user_id);
end;
$$;

create or replace function public.report_user(
    target_user_id uuid,
    report_reason text,
    report_details text default ''
)
returns void
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

    if target_user_id = current_user_id then
        raise exception 'You cannot report yourself';
    end if;

    insert into public.reports(reporter_id, reported_id, reason, details)
    values (current_user_id, target_user_id, coalesce(nullif(trim(report_reason), ''), 'Inappropriate behavior'), coalesce(report_details, ''));
end;
$$;

grant execute on function public.get_matches_for_current_user() to authenticated;
grant execute on function public.like_profile(uuid) to authenticated;
grant execute on function public.pass_profile(uuid) to authenticated;
grant execute on function public.update_privacy_settings(boolean, boolean, boolean) to authenticated;
grant execute on function public.get_events_for_current_user() to authenticated;
grant execute on function public.rsvp_event(uuid, text) to authenticated;
grant execute on function public.block_user(uuid) to authenticated;
grant execute on function public.report_user(uuid, text, text) to authenticated;

-- ─────────────────────────────────────────────────────────────────────────────
-- Row Level Security
-- ─────────────────────────────────────────────────────────────────────────────

alter table public.profiles enable row level security;
alter table public.privacy_settings enable row level security;
alter table public.profile_actions enable row level security;
alter table public.matches enable row level security;
alter table public.messages enable row level security;
alter table public.events enable row level security;
alter table public.event_rsvps enable row level security;
alter table public.blocks enable row level security;
alter table public.reports enable row level security;

drop policy if exists "Profiles are visible to signed-in users unless hidden or blocked" on public.profiles;
create policy "Profiles are visible to signed-in users unless hidden or blocked"
on public.profiles
for select
to authenticated
using (
    auth.uid() = id
    or (
        is_profile_complete = true
        and not coalesce((
            select ps.hide_profile
            from public.privacy_settings ps
            where ps.user_id = profiles.id
        ), false)
        and not public.users_are_blocked(auth.uid(), id)
        and not exists (
            select 1
            from public.profile_actions pa
            where pa.actor_id = auth.uid()
              and pa.target_id = profiles.id
        )
    )
);

drop policy if exists "Users can insert own profile" on public.profiles;
create policy "Users can insert own profile"
on public.profiles
for insert
to authenticated
with check (auth.uid() = id);

drop policy if exists "Users can update own profile" on public.profiles;
create policy "Users can update own profile"
on public.profiles
for update
to authenticated
using (auth.uid() = id)
with check (auth.uid() = id);

drop policy if exists "Users manage own privacy settings" on public.privacy_settings;
create policy "Users manage own privacy settings"
on public.privacy_settings
for all
to authenticated
using (auth.uid() = user_id)
with check (auth.uid() = user_id);

drop policy if exists "Users manage own profile actions" on public.profile_actions;
create policy "Users manage own profile actions"
on public.profile_actions
for all
to authenticated
using (auth.uid() = actor_id)
with check (auth.uid() = actor_id);

drop policy if exists "Users can view own matches" on public.matches;
create policy "Users can view own matches"
on public.matches
for select
to authenticated
using (auth.uid() = user_a_id or auth.uid() = user_b_id);

drop policy if exists "Matched users can view messages" on public.messages;
create policy "Matched users can view messages"
on public.messages
for select
to authenticated
using (
    exists (
        select 1
        from public.matches m
        where m.id = messages.chat_id
          and (m.user_a_id = auth.uid() or m.user_b_id = auth.uid())
    )
);

drop policy if exists "Matched users can send messages" on public.messages;
create policy "Matched users can send messages"
on public.messages
for insert
to authenticated
with check (
    sender_id = auth.uid()
    and exists (
        select 1
        from public.matches m
        where m.id = messages.chat_id
          and (m.user_a_id = auth.uid() or m.user_b_id = auth.uid())
    )
);

drop policy if exists "Signed-in users can view events" on public.events;
create policy "Signed-in users can view events"
on public.events
for select
to authenticated
using (true);

drop policy if exists "Admins can create events" on public.events;
create policy "Admins can create events"
on public.events
for insert
to authenticated
with check (public.current_user_is_admin());

drop policy if exists "Admins can update events" on public.events;
create policy "Admins can update events"
on public.events
for update
to authenticated
using (public.current_user_is_admin())
with check (public.current_user_is_admin());

drop policy if exists "Users manage own event RSVPs" on public.event_rsvps;
create policy "Users manage own event RSVPs"
on public.event_rsvps
for all
to authenticated
using (auth.uid() = user_id)
with check (auth.uid() = user_id);

drop policy if exists "Users manage own blocks" on public.blocks;
create policy "Users manage own blocks"
on public.blocks
for all
to authenticated
using (auth.uid() = blocker_id)
with check (auth.uid() = blocker_id);

drop policy if exists "Users can create reports" on public.reports;
create policy "Users can create reports"
on public.reports
for insert
to authenticated
with check (auth.uid() = reporter_id);

drop policy if exists "Users can view own reports" on public.reports;
create policy "Users can view own reports"
on public.reports
for select
to authenticated
using (auth.uid() = reporter_id or public.current_user_is_admin());

-- ─────────────────────────────────────────────────────────────────────────────
-- Supabase Storage buckets and policies for future image upload support
-- ─────────────────────────────────────────────────────────────────────────────

insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values
    ('profile-photos', 'profile-photos', true, 5242880, array['image/jpeg', 'image/png', 'image/webp']),
    ('event-posters', 'event-posters', true, 10485760, array['image/jpeg', 'image/png', 'image/webp'])
on conflict (id) do update set
    public = excluded.public,
    file_size_limit = excluded.file_size_limit,
    allowed_mime_types = excluded.allowed_mime_types;

drop policy if exists "Profile photos are publicly readable" on storage.objects;
create policy "Profile photos are publicly readable"
on storage.objects
for select
to authenticated, anon
using (bucket_id = 'profile-photos');

drop policy if exists "Users can upload own profile photos" on storage.objects;
create policy "Users can upload own profile photos"
on storage.objects
for insert
to authenticated
with check (
    bucket_id = 'profile-photos'
    and auth.uid()::text = (storage.foldername(name))[1]
);

drop policy if exists "Users can update own profile photos" on storage.objects;
create policy "Users can update own profile photos"
on storage.objects
for update
to authenticated
using (
    bucket_id = 'profile-photos'
    and auth.uid()::text = (storage.foldername(name))[1]
)
with check (
    bucket_id = 'profile-photos'
    and auth.uid()::text = (storage.foldername(name))[1]
);

drop policy if exists "Event posters are publicly readable" on storage.objects;
create policy "Event posters are publicly readable"
on storage.objects
for select
to authenticated, anon
using (bucket_id = 'event-posters');

drop policy if exists "Admins can upload event posters" on storage.objects;
create policy "Admins can upload event posters"
on storage.objects
for insert
to authenticated
with check (
    bucket_id = 'event-posters'
    and public.current_user_is_admin()
);

-- ─────────────────────────────────────────────────────────────────────────────
-- Realtime publication support for chat/match/event updates
-- ─────────────────────────────────────────────────────────────────────────────

do $$
begin
    if exists (select 1 from pg_publication where pubname = 'supabase_realtime') then
        if not exists (
            select 1 from pg_publication_tables
            where pubname = 'supabase_realtime'
              and schemaname = 'public'
              and tablename = 'messages'
        ) then
            alter publication supabase_realtime add table public.messages;
        end if;

        if not exists (
            select 1 from pg_publication_tables
            where pubname = 'supabase_realtime'
              and schemaname = 'public'
              and tablename = 'matches'
        ) then
            alter publication supabase_realtime add table public.matches;
        end if;

        if not exists (
            select 1 from pg_publication_tables
            where pubname = 'supabase_realtime'
              and schemaname = 'public'
              and tablename = 'event_rsvps'
        ) then
            alter publication supabase_realtime add table public.event_rsvps;
        end if;
    end if;
end $$;
