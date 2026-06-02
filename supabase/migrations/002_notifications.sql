-- DBU Connect Migration 002: Notifications table
-- This table stores in-app notifications for mutual likes, messages, etc.
-- Run this in the Supabase SQL Editor after migration 001.

-- ─────────────────────────────────────────────────────────────────────────────
-- Notifications table
-- ─────────────────────────────────────────────────────────────────────────────

create table if not exists public.notifications (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references public.profiles(id) on delete cascade,
    type text not null check (type in ('MUTUAL_LIKE', 'NEW_MESSAGE', 'PROFILE_LIKED', 'EVENT_REMINDER')),
    title text not null,
    message text not null default '',
    from_user_id uuid references public.profiles(id) on delete set null,
    from_user_name text not null default '',
    from_user_photo text not null default '',
    related_id text not null default '',
    is_read boolean not null default false,
    created_at timestamptz not null default now()
);

create index if not exists notifications_user_idx on public.notifications(user_id);
create index if not exists notifications_created_idx on public.notifications(user_id, created_at desc);

-- ─────────────────────────────────────────────────────────────────────────────
-- RLS policies for notifications
-- ─────────────────────────────────────────────────────────────────────────────

alter table public.notifications enable row level security;

drop policy if exists "Users can view own notifications" on public.notifications;
create policy "Users can view own notifications"
on public.notifications
for select
to authenticated
using (auth.uid() = user_id);

drop policy if exists "System can insert notifications" on public.notifications;
create policy "System can insert notifications"
on public.notifications
for insert
to authenticated
with check (true);

drop policy if exists "Users can update own notifications" on public.notifications;
create policy "Users can update own notifications"
on public.notifications
for update
to authenticated
using (auth.uid() = user_id)
with check (auth.uid() = user_id);

-- ─────────────────────────────────────────────────────────────────────────────
-- Trigger: Create notification when a mutual like creates a match
-- ─────────────────────────────────────────────────────────────────────────────

create or replace function public.notify_match_created()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
    user_a_name text;
    user_a_photo text;
    user_b_name text;
    user_b_photo text;
begin
    select p.name, coalesce(p.photos[1], '') into user_a_name, user_a_photo
    from public.profiles p where p.id = new.user_a_id;

    select p.name, coalesce(p.photos[1], '') into user_b_name, user_b_photo
    from public.profiles p where p.id = new.user_b_id;

    -- Notify user A about the match
    insert into public.notifications(user_id, type, title, message, from_user_id, from_user_name, from_user_photo, related_id)
    values (
        new.user_a_id,
        'MUTUAL_LIKE',
        'It''s a Match! 🎉',
        'You and ' || user_b_name || ' liked each other! Start chatting now.',
        new.user_b_id,
        user_b_name,
        user_b_photo,
        new.id::text
    );

    -- Notify user B about the match
    insert into public.notifications(user_id, type, title, message, from_user_id, from_user_name, from_user_photo, related_id)
    values (
        new.user_b_id,
        'MUTUAL_LIKE',
        'It''s a Match! 🎉',
        'You and ' || user_a_name || ' liked each other! Start chatting now.',
        new.user_a_id,
        user_a_name,
        user_a_photo,
        new.id::text
    );

    return new;
end;
$$;

drop trigger if exists matches_notify_match on public.matches;
create trigger matches_notify_match
after insert on public.matches
for each row execute function public.notify_match_created();

-- ─────────────────────────────────────────────────────────────────────────────
-- Trigger: Notify when someone likes your profile (one-way like)
-- ─────────────────────────────────────────────────────────────────────────────

create or replace function public.notify_profile_liked()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
    actor_name text;
    actor_photo text;
begin
    -- Only notify on likes, not passes
    if new.action <> 'like' then
        return new;
    end if;

    select p.name, coalesce(p.photos[1], '') into actor_name, actor_photo
    from public.profiles p where p.id = new.actor_id;

    -- Don't notify if they already matched (the match trigger handles that)
    if not exists (
        select 1 from public.matches m
        where least(m.user_a_id, m.user_b_id) = least(new.actor_id, new.target_id)
          and greatest(m.user_a_id, m.user_b_id) = greatest(new.actor_id, new.target_id)
    ) then
        if new.is_super_like then
            insert into public.notifications(user_id, type, title, message, from_user_id, from_user_name, from_user_photo, related_id)
            values (
                new.target_id,
                'PROFILE_LIKED',
                'Super Liked You! ⭐',
                actor_name || ' super liked your profile! Swipe right to match instantly.',
                new.actor_id,
                actor_name,
                actor_photo,
                new.actor_id::text
            );
        else
            insert into public.notifications(user_id, type, title, message, from_user_id, from_user_name, from_user_photo, related_id)
            values (
                new.target_id,
                'PROFILE_LIKED',
                'Someone Liked You! 💚',
                actor_name || ' liked your profile. Like them back to start chatting!',
                new.actor_id,
                actor_name,
                actor_photo,
                new.actor_id::text
            );
        end if;
    end if;

    return new;
end;
$$;

drop trigger if exists profile_actions_notify_like on public.profile_actions;
create trigger profile_actions_notify_like
after insert or update on public.profile_actions
for each row execute function public.notify_profile_liked();

-- ─────────────────────────────────────────────────────────────────────────────
-- Trigger: Notify on new message received
-- ─────────────────────────────────────────────────────────────────────────────

create or replace function public.notify_new_message()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
    sender_name text;
    sender_photo text;
    recipient_id uuid;
begin
    -- Get sender info
    select p.name, coalesce(p.photos[1], '') into sender_name, sender_photo
    from public.profiles p where p.id = new.sender_id;

    -- Determine recipient (the other user in the match)
    select case
        when m.user_a_id = new.sender_id then m.user_b_id
        else m.user_a_id
    end into recipient_id
    from public.matches m where m.id = new.chat_id;

    if recipient_id is not null then
        insert into public.notifications(user_id, type, title, message, from_user_id, from_user_name, from_user_photo, related_id)
        values (
            recipient_id,
            'NEW_MESSAGE',
            sender_name,
            case
                when length(new.text) > 50 then left(new.text, 50) || '...'
                else new.text
            end,
            new.sender_id,
            sender_name,
            sender_photo,
            new.chat_id::text
        );
    end if;

    return new;
end;
$$;

drop trigger if exists messages_notify_new on public.messages;
create trigger messages_notify_new
after insert on public.messages
for each row execute function public.notify_new_message();

-- ─────────────────────────────────────────────────────────────────────────────
-- RPC: Get notifications for current user
-- ─────────────────────────────────────────────────────────────────────────────

create or replace function public.get_notifications_for_current_user()
returns table (
    id uuid,
    type text,
    title text,
    message text,
    from_user_id uuid,
    from_user_name text,
    from_user_photo text,
    related_id text,
    is_read boolean,
    created_at timestamptz
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
        n.id,
        n.type,
        n.title,
        n.message,
        n.from_user_id,
        n.from_user_name,
        n.from_user_photo,
        n.related_id,
        n.is_read,
        n.created_at
    from public.notifications n
    where n.user_id = current_user_id
    order by n.created_at desc
    limit 50;
end;
$$;

grant execute on function public.get_notifications_for_current_user() to authenticated;

-- Add notifications table to realtime publication
do $$
begin
    if exists (select 1 from pg_publication where pubname = 'supabase_realtime') then
        if not exists (
            select 1 from pg_publication_tables
            where pubname = 'supabase_realtime'
              and schemaname = 'public'
              and tablename = 'notifications'
        ) then
            alter publication supabase_realtime add table public.notifications;
        end if;
    end if;
end $$;
