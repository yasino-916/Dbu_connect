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
            print("Creating UPDATE policy for public.matches...")
            policy_sql = """
            drop policy if exists "Matched users can update own matches" on public.matches;
            create policy "Matched users can update own matches"
            on public.matches
            for update
            to authenticated
            using (auth.uid() = user_a_id or auth.uid() = user_b_id)
            with check (auth.uid() = user_a_id or auth.uid() = user_b_id);
            """
            cur.execute(policy_sql)
            print("Successfully added update policy to public.matches!")
            
    except Exception as e:
        print(f"Error executing database update: {e}")
    finally:
        if 'conn' in locals() and conn:
            conn.close()

if __name__ == "__main__":
    main()
