import urllib.request
import json

def main():
    url = "https://hyxknslobfjabpwkcztk.supabase.co/rest/v1/profiles?select=*&is_profile_complete=eq.true"
    headers = {
        "apikey": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imh5eGtuc2xvYmZqYWJwd2tjenRrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzkwNDA3MjcsImV4cCI6MjA5NDYxNjcyN30.OecfEafgNZhD2VOAozqlDtSdypAD9s1PTd1GdO85I5A",
        "Authorization": "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imh5eGtuc2xvYmZqYWJwd2tjenRrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzkwNDA3MjcsImV4cCI6MjA5NDYxNjcyN30.OecfEafgNZhD2VOAozqlDtSdypAD9s1PTd1GdO85I5A",
        "Content-Type": "application/json",
        "User-Agent": "DBU-Connect/1.0"
    }
    
    req = urllib.request.Request(url, headers=headers, method="GET")
    try:
        with urllib.request.urlopen(req) as res:
            body = res.read().decode("utf-8")
            profiles = json.loads(body)
            print(f"Status Code: {res.status}")
            print(f"Successfully retrieved {len(profiles)} complete profiles from database:")
            for p in profiles:
                print(f"  Name: {p.get('name')} | Email: {p.get('email')} | ID: {p.get('id')}")
    except urllib.error.HTTPError as e:
        print(f"HTTP Error: {e.code} | {e.reason}")
        body = e.read().decode('utf-8')
        print(f"Error Response Body: {body}")
    except Exception as e:
        print(f"General Error: {e}")

if __name__ == "__main__":
    main()
