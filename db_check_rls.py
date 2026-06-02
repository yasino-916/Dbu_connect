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
        with conn.cursor() as cur:
            # Query RLS policies for public.profiles
            cur.execute("""
                SELECT schemaname, tablename, policyname, permissive, roles, cmd, qual, with_check 
                FROM pg_policies 
                WHERE tablename = 'profiles';
            """)
            policies = cur.fetchall()
            print("RLS Policies for 'profiles':")
            for p in policies:
                print(f"  Policy: {p[2]}")
                print(f"    Permissive: {p[3]}")
                print(f"    Roles: {p[4]}")
                print(f"    Command: {p[5]}")
                print(f"    Qual: {p[6]}")
                print(f"    With Check: {p[7]}")
                print("-" * 50)
                
            # Let's check if RLS is enabled on public.profiles
            cur.execute("""
                SELECT relrowsecurity 
                FROM pg_class 
                WHERE relname = 'profiles';
            """)
            rls_enabled = cur.fetchone()
            print(f"\nRow Level Security enabled on 'profiles': {rls_enabled[0]}")
    except Exception as e:
        print(f"Error: {e}")
    finally:
        if 'conn' in locals() and conn:
            conn.close()

if __name__ == "__main__":
    main()
