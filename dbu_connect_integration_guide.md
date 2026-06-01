# DBU Connect Integration Guide
### Syncing the Mobile App & Administrator Web Dashboard

This guide explains how your **Android Mobile App** and the **Vercel Admin Web Dashboard** are securely connected, how data flows in real-time between them, and the steps required to verify that they are targeting the same live database.

---

## 1. System Architecture: Shared Supabase Backend

There is no intermediate custom server between your mobile app and the website. Instead, **both applications act as clients connecting directly to the same Supabase Cloud project (`hyxknslobfjabpwkcztk`)**. 

Supabase automatically acts as your shared API gateway. When you modify database tables, Supabase instantly exposes secure REST endpoints that both the website (via JavaScript SDK) and the Android app (via Retrofit/OkHttp) query.

```mermaid
graph TD
    subgraph "Vercel Web Dashboard (React/Vite)"
        A[Admin Web Dashboard]
    end
    
    subgraph "Android Client (Kotlin/Retrofit)"
        B[DBU Connect Mobile App]
    end

    subgraph "Shared Supabase Backend (Cloud)"
        C[Supabase REST API Gateway]
        D[(PostgreSQL Database)]
        E[Supabase Auth]
        F[Storage Buckets]
        C --> D
        C --> E
        C --> F
    end

    A -- "Read/Write via JS SDK" --> C
    B -- "Read/Write via Retrofit" --> C
```

---

## 2. Key Data Synchronization Flows

Here is exactly how the app and website interact through the shared database tables:

### A. Campus Events Flow
1. **Creation**: An administrator logs into the [Vercel Dashboard](https://dbu-connect-web-dashboard.vercel.app/) and fills out the Event form (Title, Description, DateTime, Location, Image, Tags).
2. **Insertion**: The React dashboard inserts a new record into the `events` table via:
   ```javascript
   await supabase.from('events').insert([eventPayload]);
   ```
3. **Retrieval**: When a student opens the DBU Connect Android app, the `SupabaseApiService` runs:
   ```kotlin
   override suspend fun getEvents(): Result<List<Event>> = runCatching {
       api.getEventsForCurrentUser().map { it.toEvent() }
   }
   ```
   This executes the Postgres RPC function `get_events_for_current_user()` and displays the brand-new event on the student's feed instantly.

### B. Event RSVPs Flow
1. **RSVP Action**: A student clicks **"Going"** on an event in the mobile app.
2. **Sync**: The Android app calls the `rsvp_event(eventId, "GOING")` RPC in Supabase, updating the `event_rsvps` table.
3. **Dashboard Real-Time Update**: 
   * The web dashboard's **Attendees Modal** queries this table dynamically to show the list of participating students:
     ```javascript
     const { data } = await supabase.from('event_rsvps').select('status, profiles(...)').eq('event_id', event.id);
     ```
   * The RSVP count on the admin dashboard increments immediately!

### C. Student Moderation Flow
1. **Report Submission**: A student reports another user for inappropriate behavior on the app.
2. **Log**: The Android app calls `report_user` in `SupabaseApiService`, inserting a report record into the `reports` table.
3. **Dashboard Notification**: The administrator clicks the **Moderation** tab on the web dashboard. The web app queries the `reports` table:
   ```javascript
   const { data: dbReports } = await supabase.from('reports').select('*').order('created_at', { ascending: false });
   ```
   The admin sees the student's details, the reason for reporting, and can click **Reviewed**, **Dismissed**, or **Actioned** to manage community safety.

---

## 3. Configuration Step: Deploying to Vercel with API Keys

Because `.env` files are ignored from version control for security reasons, you must explicitly tell Vercel to use your live Supabase database credentials. If these are not configured, your deployed website won't connect to the database.

### How to Configure Variables in Vercel:
1. Log in to your [Vercel Account](https://vercel.com).
2. Open your project **`dbu-connect-web-dashboard`**.
3. Go to **Settings** ➔ **Environment Variables**.
4. Add the following two key-value pairs:

| Name | Value |
| :--- | :--- |
| **`VITE_SUPABASE_URL`** | `https://hyxknslobfjabpwkcztk.supabase.co` |
| **`VITE_SUPABASE_ANON_KEY`** | `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imh5eGtuc2xvYmZqYWJwd2tjenRrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzkwNDA3MjcsImV4cCI6MjA5NDYxNjcyN30.OecfEafgNZhD2VOAozqlDtSdypAD9s1PTd1GdO85I5A` |

5. Click **Save** and trigger a **Redeploy** on Vercel so the environment changes are compiled.

Once configured, the header in your deployed dashboard will show a **"Connected Live"** green badge, proving that it is successfully reading and writing to your production database.

---

## 4. Granting Administrator Privileges in Supabase

To log into the administrator hub, the user's account must have the `is_admin` flag set to `true` inside the `profiles` table.

If you sign up a new account or want to upgrade an existing user to an administrator:
1. Go to the [Supabase SQL Editor](https://supabase.com/dashboard/project/hyxknslobfjabpwkcztk/sql/new).
2. Run the following SQL query to grant admin access (replace the email with your admin's registered email address):

```sql
UPDATE public.profiles
SET is_admin = true
WHERE email = 'gech@dbu.edu.et'; -- Replace with the actual email
```

---

> [!TIP]
> Both of your projects (Android & Web Dashboard) are configured perfectly to use the **same API and credentials**. Once you configure the environment variables on Vercel and set `is_admin = true` for your administrator profiles, the system will operate as a fully integrated, real-time ecosystem!
