import psycopg2
import uuid
import time

def main():
    db_config = {
        "host": "aws-0-eu-west-1.pooler.supabase.com",
        "port": 6543,
        "database": "postgres",
        "user": "postgres.hyxknslobfjabpwkcztk",
        "password": "GecH    123e4r"
    }

    mock_profiles = [
        {
            "email": "gelila@dbu.edu.et",
            "recovery_email": "gelila_recovery@gmail.com",
            "name": "Gelila Birhanu",
            "age": 21,
            "department": "Medicine (MBBS)",
            "year": 3,
            "interests": ["Music", "Fitness", "Reading", "Coffee"],
            "bio": "Medical student at Asrat Woldeyes campus. Usually at the library studying or listening to classic music. Let's grab hot tea to survive the chilly Debre Berhan weather!",
            "intent": "Friends",
            "photos": ["https://th.bing.com/th/id/OIP.RGbLuSEhB05DSFGoOmM2WAHaLB?w=137&h=204&c=7&r=0&o=7&dpr=1.3&pid=1.7&rm=3"]
        },
        {
            "email": "abel@dbu.edu.et",
            "recovery_email": "abel_recovery@gmail.com",
            "name": "Abel Tekle",
            "age": 22,
            "department": "Software Engineering",
            "year": 4,
            "interests": ["Technology", "Coffee", "Gaming", "Studying"],
            "bio": "Software Engineering senior. Usually coding at the Computing Lab or finding the best Macchiato spot on campus. Let's team up for final projects!",
            "intent": "Study Buddy",
            "photos": ["https://th.bing.com/th/id/OIP.q2WRCzdPGtxBzBflzkntfgHaJ4?w=208&h=277&c=7&r=0&o=7&dpr=1.3&pid=1.7&rm=3"]
        },
        {
            "email": "bethlehem@dbu.edu.et",
            "recovery_email": "bethlehem_recovery@gmail.com",
            "name": "Bethlehem Kassahun",
            "age": 20,
            "department": "Accounting and Finance",
            "year": 2,
            "interests": ["Reading", "Photography", "Art", "Travel"],
            "bio": "Sophomore studying Accounting. Love reading Ethiopian fiction and photography. Let's find beautiful spots around DBU to take cool pictures!",
            "intent": "Friends",
            "photos": ["https://th.bing.com/th/id/OIP._QXRJiDeZ-aNSD7Vmuq0MwHaLH?w=204&h=306&c=7&r=0&o=7&dpr=1.3&pid=1.7&rm=3"]
        },
        {
            "email": "naod@dbu.edu.et",
            "recovery_email": "naod_recovery@gmail.com",
            "name": "Naod Kassahun",
            "age": 23,
            "department": "Civil Engineering",
            "year": 5,
            "interests": ["Sports", "Coffee", "Travel", "Movies"],
            "bio": "Civil Engineering graduating student. Passionate about structural design. Love football, hiking around the beautiful Debre Berhan hills, and deep conversations.",
            "intent": "Dating",
            "photos": ["https://th.bing.com/th/id/OIP.P_vFj2DrVQ1TQ_0G2z-x6gHaJQ?w=152&h=190&c=7&r=0&o=7&dpr=1.3&pid=1.7&rm=3"]
        },
        {
            "email": "senait@dbu.edu.et",
            "recovery_email": "senait_recovery@gmail.com",
            "name": "Senait Hailu",
            "age": 22,
            "department": "Law (LLB)",
            "year": 4,
            "interests": ["Volunteering", "Reading", "Debate", "Studying"],
            "bio": "Future lawyer. Love debating, volunteering, and learning about human rights. Looking for friends who enjoy good books and warm tea on campus.",
            "intent": "Friends",
            "photos": ["https://th.bing.com/th?q=Ethiopian+Straight+Hair&w=120&h=120&c=1&rs=1&qlt=70&o=7&cb=1&dpr=1.3&pid=InlineBlock&rm=3&mkt=en-WW&cc=ET&setlang=en&adlt=strict&t=1&mw=247"]
        },
        {
            "email": "kirubel@dbu.edu.et",
            "recovery_email": "kirubel_recovery@gmail.com",
            "name": "Kirubel Worku",
            "age": 21,
            "department": "Computer Science",
            "year": 3,
            "interests": ["Technology", "Coffee", "Debate", "Gaming"],
            "bio": "Computer Science junior. Building dynamic mobile apps. Let's debate about tech or find the best spots to grab a snack near DBU main gate!",
            "intent": "Study Buddy",
            "photos": ["https://th.bing.com/th/id/OIP.P_vFj2DrVQ1TQ_0G2z-x6gHaJQ?w=152&h=190&c=7&r=0&o=7&dpr=1.3&pid=1.7&rm=3"]
        },
        {
            "email": "kidist@dbu.edu.et",
            "recovery_email": "kidist_recovery@gmail.com",
            "name": "Kidist Lemma",
            "age": 20,
            "department": "Environmental Science",
            "year": 2,
            "interests": ["Nature", "Volunteering", "Travel", "Movies"],
            "bio": "Nature lover studying Environmental Science. Passionate about green campus initiatives. Let's hang out near the DBU main quad or plant some trees!",
            "intent": "Friends",
            "photos": ["https://th.bing.com/th/id/OIP._QXRJiDeZ-aNSD7Vmuq0MwHaLH?w=204&h=306&c=7&r=0&o=7&dpr=1.3&pid=1.7&rm=3"]
        },
        {
            "email": "yared@dbu.edu.et",
            "recovery_email": "yared_recovery@gmail.com",
            "name": "Yared Kassaye",
            "age": 22,
            "department": "Mechanical Engineering",
            "year": 4,
            "interests": ["Sports", "Music", "Fitness", "Nature"],
            "bio": "Mechanical Engineering senior. Loves sports, playing acoustic guitar, and fitness. Always up for an outdoor running session in the cold DB morning air!",
            "intent": "Dating",
            "photos": ["https://th.bing.com/th/id/OIP.q2WRCzdPGtxBzBflzkntfgHaJ4?w=208&h=277&c=7&r=0&o=7&dpr=1.3&pid=1.7&rm=3"]
        },
        {
            "email": "eyerusalem@dbu.edu.et",
            "recovery_email": "eyerusalem_recovery@gmail.com",
            "name": "Eyerusalem Negash",
            "age": 21,
            "department": "Pharmacy",
            "year": 4,
            "interests": ["Music", "Cooking", "Studying", "Coffee"],
            "bio": "Pharmacy student. Almost done! Love baking, listening to classic Ethiopian music, and chatting about healthcare. Let's be friends!",
            "intent": "Study Buddy",
            "photos": ["https://th.bing.com/th/id/OIP.RGbLuSEhB05DSFGoOmM2WAHaLB?w=137&h=204&c=7&r=0&o=7&dpr=1.3&pid=1.7&rm=3"]
        },
        {
            "email": "mikias@dbu.edu.et",
            "recovery_email": "mikias_recovery@gmail.com",
            "name": "Mikias Solomon",
            "age": 21,
            "department": "Economics",
            "year": 3,
            "interests": ["Gaming", "Movies", "Sports", "Coffee"],
            "bio": "Economics junior. Let's talk about finance, watch action movies, or play a quick FIFA match. Looking for chill friends around DBU!",
            "intent": "Friends",
            "photos": ["https://th.bing.com/th/id/OIP.P_vFj2DrVQ1TQ_0G2z-x6gHaJQ?w=152&h=190&c=7&r=0&o=7&dpr=1.3&pid=1.7&rm=3"]
        }
    ]

    mock_events = [
        {
            "title": "Annual Engineering Social",
            "description": "Join us for the Annual Engineering Social! This is a great opportunity to connect with fellow students, faculty, and local industry professionals in a relaxed setting. Enjoy free food, outdoor games, and a chance to learn about upcoming department projects.",
            "date_time": int((time.time() + 86400 * 3) * 1000),
            "end_time": int((time.time() + 86400 * 3 + 10800) * 1000),
            "location": "Main Quad",
            "image_url": "https://images.unsplash.com/photo-1523580494863-6f3031224c94?w=600",
            "tags": ["Engineering", "Social", "Networking"]
        },
        {
            "title": "Campus Coffee Meetup",
            "description": "A casual coffee meetup for all students. Meet new friends, discover study partners, and enjoy great conversations over freshly brewed coffee.",
            "date_time": int((time.time() + 86400 * 5) * 1000),
            "end_time": None,
            "location": "Student Center Café",
            "image_url": "https://images.unsplash.com/photo-1511920170033-f8396924c348?w=600",
            "tags": ["Social", "Coffee", "Casual"]
        },
        {
            "title": "Study Group: Data Structures",
            "description": "Weekly study group for Data Structures and Algorithms. All CS students welcome!",
            "date_time": int((time.time() + 86400 * 2) * 1000),
            "end_time": None,
            "location": "Library Room 204",
            "image_url": "https://images.unsplash.com/photo-1522202176988-66273c2fd55f?w=600",
            "tags": ["Study", "CS", "Academic"]
        },
        {
            "title": "Sports Day: Volleyball Tournament",
            "description": "Annual inter-department volleyball tournament. Form your team and compete!",
            "date_time": int((time.time() + 86400 * 7) * 1000),
            "end_time": None,
            "location": "Sports Complex",
            "image_url": "https://images.unsplash.com/photo-1574629810360-7efbbe195018?w=600",
            "tags": ["Sports", "Tournament", "Fun"]
        }
    ]

    try:
        conn = psycopg2.connect(**db_config)
        conn.autocommit = False
        cur = conn.cursor()

        print("Seeding profiles and events to database...")

        # 1. Clean existing mock data if any
        emails_to_clean = [p["email"] for p in mock_profiles]
        cur.execute("DELETE FROM auth.users WHERE email = ANY(%s)", (emails_to_clean,))
        print(f"Cleaned up {cur.rowcount} existing auth.users entries.")

        # 2. Insert mock profiles
        for p in mock_profiles:
            user_id = str(uuid.uuid4())
            
            # Insert into auth.users
            cur.execute("""
                INSERT INTO auth.users (
                    id, instance_id, aud, role, email, encrypted_password, 
                    email_confirmed_at, raw_app_meta_data, 
                    raw_user_meta_data, is_sso_user, is_anonymous, created_at, updated_at
                ) VALUES (
                    %s, '00000000-0000-0000-0000-000000000000', 'authenticated', 'authenticated', 
                    %s, crypt('password123', gen_salt('bf')), now(), 
                    '{"provider": "email", "providers": ["email"]}', 
                    %s, false, false, now(), now()
                )
            """, (
                user_id, 
                p["email"], 
                f'{{"name": "{p["name"]}", "recovery_email": "{p["recovery_email"]}"}}'
            ))

            # Insert into public.profiles
            cur.execute("""
                INSERT INTO public.profiles (
                    id, name, age, department, year, bio, photos, interests, intent, email, is_profile_complete, created_at, updated_at
                ) VALUES (
                    %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, true, now(), now()
                )
            """, (
                user_id,
                p["name"],
                p["age"],
                p["department"],
                p["year"],
                p["bio"],
                p["photos"],
                p["interests"],
                p["intent"],
                p["email"]
            ))

            # Insert into public.privacy_settings
            cur.execute("""
                INSERT INTO public.privacy_settings (
                    user_id, show_department, show_year, hide_profile, created_at, updated_at
                ) VALUES (
                    %s, true, true, false, now(), now()
                )
            """, (user_id,))

        print("Seeded profiles successfully.")

        # 3. Clean and insert events
        cur.execute("DELETE FROM public.events WHERE created_by IS NULL")
        print(f"Cleaned up {cur.rowcount} system events.")

        for event in mock_events:
            cur.execute("""
                INSERT INTO public.events (
                    id, title, description, date_time, end_time, location, image_url, tags, rsvp_status, attendee_count, attendees_from_dept, created_at, updated_at
                ) VALUES (
                    gen_random_uuid(), %s, %s, %s, %s, %s, %s, %s, 'NONE', 0, 0, now(), now()
                )
            """, (
                event["title"],
                event["description"],
                event["date_time"],
                event["end_time"],
                event["location"],
                event["image_url"],
                event["tags"]
            ))

        print("Seeded events successfully.")

        conn.commit()
        print("All database seed transactions committed successfully!")

    except Exception as e:
        if 'conn' in locals() and conn:
            conn.rollback()
        print(f"Seeding failed: {e}")
    finally:
        if 'conn' in locals() and conn:
            conn.close()

if __name__ == "__main__":
    main()
