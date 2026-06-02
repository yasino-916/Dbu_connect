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
            print("Adding message columns (reply_to_id, reply_to_text, is_forwarded, is_edited) to public.messages...")
            
            # Add columns if they do not exist
            alter_sql = """
            ALTER TABLE public.messages ADD COLUMN IF NOT EXISTS reply_to_id uuid;
            ALTER TABLE public.messages ADD COLUMN IF NOT EXISTS reply_to_text text;
            ALTER TABLE public.messages ADD COLUMN IF NOT EXISTS is_forwarded boolean DEFAULT false;
            ALTER TABLE public.messages ADD COLUMN IF NOT EXISTS is_edited boolean DEFAULT false;
            """
            cur.execute(alter_sql)
            print("Successfully added new columns to public.messages!")
            
    except Exception as e:
        print(f"Error executing database update: {e}")
    finally:
        if 'conn' in locals() and conn:
            conn.close()

if __name__ == "__main__":
    main()
