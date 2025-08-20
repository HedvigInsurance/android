import requests
import re

"""
Script that processes keys that we know are unused in android
and removes "android" platform for them in localise project.
Skips keys with only "android" platform and saves them to a separate
file, as they would need to be deleted/reassigned to other platform.

property KEY_LIST_FILE The file containing unused keys to process
property NUM_KEYS_TO_PROCESS Number of keys to process in each batch
property DRY_RUN Whether to run in test mode without making changes

sample
// Changes "platform": ["android", "other"] to "platform": ["other"]
// Skips "platform": ["android"] and saves to keys_with_empty_platforms.txt
"""

API_TOKEN = "replace_with_token"
PROJECT_ID = "replace_with_project_id"
KEY_LIST_FILE = "unused_keys.txt"
DRY_RUN = False
NUM_KEYS_TO_PROCESS = 100

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
    
    batch = all_keys[:NUM_KEYS_TO_PROCESS]
    remaining = all_keys[NUM_KEYS_TO_PROCESS:]
    
    if not batch:
        return
    
    print(f"🚀 Processing {len(batch)} keys (DRY_RUN={DRY_RUN})...\n")
    
    keys_to_update = []
    keys_with_empty_platforms = []
    
    for original_key in batch:
        key_id, platforms = get_key_info(original_key)
        
        if key_id is None:
            print(f"⛔ Was not found: {original_key}")
            continue
        
        if not platforms or "android" not in platforms:
            continue
        
        # Remove "android" from platforms, keep all other platforms
        updated_platforms = [p for p in platforms if p != "android"]
        
        # Check if this would result in empty platforms
        if not updated_platforms:
            keys_with_empty_platforms.append({
                "key_name": original_key,
                "original_platforms": platforms,
                "key_id": key_id
            })
            print(f"⚠️  Would result in empty platforms: '{original_key}' {platforms} → {updated_platforms}")
            continue
        
        if DRY_RUN:
            print(f"📝 Dry-run: '{original_key}' {platforms} → {updated_platforms}")
        else:
            print(f"✅ Will update: '{original_key}' {platforms} → {updated_platforms}")
            keys_to_update.append({
                "key_id": key_id,
                "platforms": updated_platforms
            })
    
    if not DRY_RUN and keys_to_update:
        update_platforms_batch(keys_to_update)
    
    # Save keys with potentially empty platforms to a separate file to decide on them separately
    if keys_with_empty_platforms:
        with open("keys_with_empty_platforms.txt", "a") as f:
            for key_info in keys_with_empty_platforms:
                f.write(f"{key_info['key_name']}\n")
        print(f"\n⚠️  {len(keys_with_empty_platforms)} keys would result in empty platforms - appended to 'keys_with_empty_platforms.txt'")
    
    with open(KEY_LIST_FILE, "w") as f:
        for key in remaining:
            f.write(key + "\n")
    
    print(f"\n📄 Updated {len(batch)} keys. {len(remaining)} keys left in {KEY_LIST_FILE}.")

if __name__ == "__main__":
    main() 