import requests
import re

"""
This script reads keys from found_keys.txt, identifies those with only "android"
as their platform, and changes the platform from "android" to "other".
Used for the keys found in database elsewhere but not in android project.
"""

API_TOKEN = "replace_with_token"
PROJECT_ID = "replace_with_project_id"
KEY_LIST_FILE = "found_keys.txt"
DRY_RUN = True

headers = {
    "X-Api-Token": API_TOKEN,
    "Content-Type": "application/json"
}

def get_key_info(key_name):
    url = f"https://api.lokalise.com/api2/projects/{PROJECT_ID}/keys"
    params = {"filter_keys": key_name}
    resp = requests.get(url, headers=headers, params=params)
    resp.raise_for_status()
    keys = resp.json().get("keys", [])
    if not keys:
        return None, None
    key = keys[0]
    return key["key_id"], key.get("platforms", [])

def update_platforms_batch(keys_to_update):
    url = f"https://api.lokalise.com/api2/projects/{PROJECT_ID}/keys"
    data = {"keys": keys_to_update}
    resp = requests.put(url, headers=headers, json=data)
    if resp.status_code == 200:
        print(f"✅ Updated {len(keys_to_update)} keys.")
    else:
        print(f"❌ Error – Status: {resp.status_code}")
        print(resp.text)

def main():
    with open(KEY_LIST_FILE, "r") as f:
        all_keys = [line.strip() for line in f if line.strip()]
    
    if not all_keys:
        print("No keys found in the file.")
        return
    
    print(f"🚀 Processing {len(all_keys)} keys from {KEY_LIST_FILE} (DRY_RUN={DRY_RUN})...\n")
    
    keys_to_update = []
    keys_processed = 0
    keys_skipped = 0
    
    for key_name in all_keys:
        key_id, platforms = get_key_info(key_name)
        
        if key_id is None:
            print(f"⛔ Key not found: {key_name}")
            keys_skipped += 1
            continue
        
        # Check if "android" is the only platform
        if platforms == ["android"]:
            # Change "android" to "other"
            updated_platforms = ["other"]
            
            if DRY_RUN:
                print(f"📝 Dry-run: '{key_name}' {platforms} → {updated_platforms}")
            else:
                print(f"✅ Will update: '{key_name}' {platforms} → {updated_platforms}")
                keys_to_update.append({
                    "key_id": key_id,
                    "platforms": updated_platforms
                })
            keys_processed += 1
        else:
            print(f"⏭️  Skipped: '{key_name}' has platforms: {platforms} (not just 'android')")
            keys_skipped += 1
    
    if not DRY_RUN and keys_to_update:
        update_platforms_batch(keys_to_update)
    
    print(f"\n📊 Summary:")
    print(f"   - Total keys: {len(all_keys)}")
    print(f"   - Keys with only 'android' platform: {keys_processed}")
    print(f"   - Keys skipped: {keys_skipped}")
    print(f"   - Keys updated: {len(keys_to_update) if not DRY_RUN else 0}")

if __name__ == "__main__":
    main() 