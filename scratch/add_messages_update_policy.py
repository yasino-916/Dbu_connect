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
            print("Creating UPDATE policy for public.messages...")
            policy_sql = """
            drop policy if exists "Matched users can update message status" on public.messages;
            create policy "Matched users can update message status"
            on public.messages
            for update
            to authenticated
            using (
                exists (
                    select 1
                    from public.matches m
                    where m.id = messages.chat_id
                      and (m.user_a_id = auth.uid() or m.user_b_id = auth.uid())
                )
            )
            with check (
                exists (
                    select 1
                    from public.matches m
                    where m.id = messages.chat_id
                      and (m.user_a_id = auth.uid() or m.user_b_id = auth.uid())
                )
            );
            """
            cur.execute(policy_sql)
            print("Successfully added update policy to public.messages!")
            
    except Exception as e:
        print(f"Error executing database update: {e}")
    finally:
        if 'conn' in locals() and conn:
            conn.close()

if __name__ == "__main__":
    main()
