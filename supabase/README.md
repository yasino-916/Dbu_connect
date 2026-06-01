# DBU Connect Supabase Backend

This directory contains the SQL backend setup for DBU Connect.

## Migration

Apply `migrations/001_initial_backend.sql` in the Supabase SQL editor, or run it from a secure terminal with `psql`.

Do **not** put the Postgres connection string or database password in the Android app. The mobile app should only use:

- `SUPABASE_URL`
- `SUPABASE_ANON_KEY`

These are read by `app/build.gradle.kts` from `local.properties` or environment variables.

## What the migration creates

- User profiles
- Privacy settings
- Likes/passes
- Mutual-match creation
- Matches and messages
- Events and per-user RSVPs
- Blocks and reports
- RLS policies
- Supabase Storage buckets/policies for profile photos and event posters
- Realtime publication entries for messages, matches, and RSVPs
