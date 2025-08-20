#!/usr/bin/env python3
"""Script to find unused string keys in an Android project.

This script analyzes Android string resources and identifies unused string keys
by scanning Kotlin files for usage patterns. It supports various string reference
formats including R.string.key, stringResource(), and hedvig.resources.R.string.key.

Usage:
    python find_unused_strings_advanced.py [--project-root PATH] [--output FILE] [--json FILE] [--csv FILE] [--verbose]

Args:
    --project-root: Path to project root (default: current directory)
    --output: Output file for text report
    --json: Output file for JSON report  
    --csv: Output file for CSV export of unused strings
    --verbose: Enable verbose output

Returns:
    0 if no unused strings found, 1 if unused strings exist
"""

import os
import re
import xml.etree.ElementTree as ET
from pathlib import Path
from typing import Set, Dict, List, Tuple, Optional
import argparse
import json


class StringUsageAnalyzer:
    def __init__(self, project_root: str, verbose: bool = False):
        self.project_root = project_root
        self.verbose = verbose
        self.string_files = []
        self.all_strings = {}
        self.used_keys = set()
        self.unused_keys = set()
        
        # Patterns to search for string usage
        self.usage_patterns = [
            r'R\.string\.(\w+)',  # R.string.key_name
            r'stringResource\(R\.string\.(\w+)\)',  # stringResource(R.string.key_name)
            r'stringResource\(id\s*=\s*R\.string\.(\w+)\)',  # stringResource(id = R.string.key_name)
            r'hedvig\.resources\.R\.string\.(\w+)',  # hedvig.resources.R.string.key_name
            r'stringResource\(hedvig\.resources\.R\.string\.(\w+)\)',  # stringResource(hedvig.resources.R.string.key_name)
            r'stringResource\(id\s*=\s*hedvig\.resources\.R\.string\.(\w+)\)',  # stringResource(id = hedvig.resources.R.string.key_name)
            # Add support for string arrays and plurals
            r'stringArrayResource\(R\.string\.(\w+)\)',  # stringArrayResource(R.string.key_name)
            r'stringArrayResource\(id\s*=\s*R\.string\.(\w+)\)',  # stringArrayResource(id = R.string.key_name)
            r'pluralStringResource\(R\.string\.(\w+)',  # pluralStringResource(R.string.key_name, ...)
            r'pluralStringResource\(id\s*=\s*R\.string\.(\w+)',  # pluralStringResource(id = R.string.key_name, ...)
            r'hedvig\.resources\.R\.string\.(\w+)',  # hedvig.resources.R.string.key_name (already covered but keeping for clarity)
            r'stringArrayResource\(hedvig\.resources\.R\.string\.(\w+)\)',  # stringArrayResource(hedvig.resources.R.string.key_name)
            r'stringArrayResource\(id\s*=\s*hedvig\.resources\.R\.string\.(\w+)\)',  # stringArrayResource(id = hedvig.resources.R.string.key_name)
            r'pluralStringResource\(hedvig\.resources\.R\.string\.(\w+)',  # pluralStringResource(hedvig.resources.R.string.key_name, ...)
            r'pluralStringResource\(id\s*=\s*hedvig\.resources\.R\.string\.(\w+)',  # pluralStringResource(id = hedvig.resources.R.string.key_name, ...)
        ]
        
        # Compile regex patterns for better performance
        self.compiled_patterns = [re.compile(pattern, re.MULTILINE) for pattern in self.usage_patterns]

    def parse_strings_xml(self, file_path: str) -> Dict[str, str]:
        """Parse a strings.xml file and extract string key-value pairs."""
        try:
            tree = ET.parse(file_path)
            root = tree.getroot()
            
            strings = {}
            for string_elem in root.findall('.//string'):
                name = string_elem.get('name')
                if name:
                    # Get text content, handling nested elements
                    text = string_elem.text or ""
                    for child in string_elem:
                        if child.text:
                            text += child.text
                        if child.tail:
                            text += child.tail
                    strings[name] = text.strip()
            
            if self.verbose:
                print(f"  Parsed {len(strings)} strings from {file_path}")
            
            return strings
            
        except ET.ParseError as e:
            print(f"Warning: Could not parse {file_path}: {e}")
            return {}
        except Exception as e:
            print(f"Warning: Error reading {file_path}: {e}")
            return {}

    def find_string_resource_files(self) -> List[str]:
        """Find all strings.xml files in the project."""
        string_files = []
        
        for root, dirs, files in os.walk(self.project_root):
            # Skip build directories and git
            dirs[:] = [d for d in dirs if d not in ['build', '.git', '.gradle', '.kotlin', 'build-logic', 'node_modules']]
            
            for file in files:
                if file == 'strings.xml':
                    string_files.append(os.path.join(root, file))
        
        return string_files

    def search_string_usage_in_file(self, file_path: str) -> Set[str]:
        """Search for string key usage in a single file using regex patterns."""
        used_keys = set()
        
        try:
            with open(file_path, 'r', encoding='utf-8', errors='ignore') as f:
                content = f.read()
            
            # Search using compiled regex patterns
            for pattern in self.compiled_patterns:
                matches = pattern.findall(content)
                for match in matches:
                    # Check if this matches any of our string keys (including dot-to-underscore conversion)
                    if match in self.all_strings:
                        used_keys.add(match)
                    else:
                        # Check if this is a dot-converted version of any key
                        # Convert dots to underscores in the original keys and check for matches
                        for original_key in self.all_strings.keys():
                            if original_key.replace('.', '_') == match:
                                used_keys.add(original_key)
                                if self.verbose:
                                    print(f"    Found dot-converted key: {original_key} -> {match}")
            
        except Exception as e:
            if self.verbose:
                print(f"Warning: Could not read {file_path}: {e}")
        
        return used_keys

    def search_string_usage_in_project(self) -> Set[str]:
        """Search for string key usage across all Kotlin files in the project."""
        used_keys = set()
        processed_files = 0
        
        for root, dirs, files in os.walk(self.project_root):
            # Skip build directories and git
            dirs[:] = [d for d in dirs if d not in ['build', '.git', '.gradle', '.kotlin', 'build-logic', 'node_modules']]
            
            for file in files:
                if file.endswith('.kt'):
                    file_path = os.path.join(root, file)
                    file_used_keys = self.search_string_usage_in_file(file_path)
                    used_keys.update(file_used_keys)
                    processed_files += 1
                    
                    if self.verbose and file_used_keys:
                        print(f"  Found {len(file_used_keys)} used strings in {file_path}")
        
        if self.verbose:
            print(f"  Processed {processed_files} Kotlin files")
        
        return used_keys

    def analyze_strings(self) -> Tuple[Dict[str, str], Set[str], Set[str]]:
        """Analyze string resources and find unused keys."""
        print("🔍 Finding string resource files...")
        self.string_files = self.find_string_resource_files()
        print(f"Found {len(self.string_files)} string resource files")
        
        # Parse all string resource files
        for file_path in self.string_files:
            if self.verbose:
                print(f"Parsing {file_path}")
            strings = self.parse_strings_xml(file_path)
            self.all_strings.update(strings)
        
        print(f"Total string keys found: {len(self.all_strings)}")
        
        # Search for usage in Kotlin files
        print("🔍 Searching for string usage in Kotlin files...")
        self.used_keys = self.search_string_usage_in_project()
        
        # Find unused keys
        self.unused_keys = set(self.all_strings.keys()) - self.used_keys
        
        return self.all_strings, self.used_keys, self.unused_keys

    def generate_detailed_report(self, output_file: str = None, json_output: str = None) -> str:
        """Generate a detailed report of the analysis."""
        report_lines = []
        
        report_lines.append("=" * 80)
        report_lines.append("STRING RESOURCE USAGE ANALYSIS REPORT")
        report_lines.append("=" * 80)
        report_lines.append("")
        
        report_lines.append(f"Project: {self.project_root}")
        report_lines.append(f"String resource files: {len(self.string_files)}")
        report_lines.append(f"Total string keys found: {len(self.all_strings)}")
        report_lines.append(f"Used string keys: {len(self.used_keys)}")
        report_lines.append(f"Unused string keys: {len(self.unused_keys)}")
        report_lines.append("")
        
        # Usage statistics
        if self.all_strings:
            usage_rate = (len(self.used_keys) / len(self.all_strings)) * 100
            report_lines.append("USAGE STATISTICS:")
            report_lines.append("-" * 40)
            report_lines.append(f"Usage rate: {usage_rate:.1f}%")
            report_lines.append(f"Unused rate: {100 - usage_rate:.1f}%")
            report_lines.append("")
        
        if self.unused_keys:
            report_lines.append("UNUSED STRING KEYS:")
            report_lines.append("-" * 40)
            for key in sorted(self.unused_keys):
                value = self.all_strings.get(key, "")
                # Truncate long values for readability
                if len(value) > 100:
                    value = value[:100] + "..."
                report_lines.append(f"  {key}: {value}")
            report_lines.append("")
        else:
            report_lines.append("✅ No unused string keys found!")
            report_lines.append("")
        
        # Show some examples of used keys
        if self.used_keys:
            report_lines.append("EXAMPLES OF USED STRING KEYS:")
            report_lines.append("-" * 40)
            used_keys_list = sorted(list(self.used_keys))
            for key in used_keys_list[:10]:  # Show first 10
                value = self.all_strings.get(key, "")
                if len(value) > 80:
                    value = value[:80] + "..."
                report_lines.append(f"  {key}: {value}")
            if len(self.used_keys) > 10:
                report_lines.append(f"  ... and {len(self.used_keys) - 10} more")
            report_lines.append("")
        
        # String resource files summary
        report_lines.append("STRING RESOURCE FILES:")
        report_lines.append("-" * 40)
        for file_path in sorted(self.string_files):
            relative_path = os.path.relpath(file_path, self.project_root)
            report_lines.append(f"  {relative_path}")
        report_lines.append("")
        
        report = "\n".join(report_lines)
        
        # Print to console
        print(report)
        
        # Save to text file if specified
        if output_file:
            try:
                with open(output_file, 'w', encoding='utf-8') as f:
                    f.write(report)
                print(f"📄 Text report saved to: {output_file}")
            except Exception as e:
                print(f"Warning: Could not save text report to {output_file}: {e}")
        
        # Save to JSON file if specified
        if json_output:
            try:
                json_data = {
                    "project_root": self.project_root,
                    "string_files": [os.path.relpath(f, self.project_root) for f in self.string_files],
                    "total_strings": len(self.all_strings),
                    "used_strings": len(self.used_keys),
                    "unused_strings": len(self.unused_keys),
                    "usage_rate": (len(self.used_keys) / len(self.all_strings)) * 100 if self.all_strings else 0,
                    "unused_keys": sorted(list(self.unused_keys)),
                    "used_keys": sorted(list(self.used_keys)),
                    "all_strings": self.all_strings
                }
                
                with open(json_output, 'w', encoding='utf-8') as f:
                    json.dump(json_data, f, indent=2, ensure_ascii=False)
                print(f"📄 JSON report saved to: {json_output}")
            except Exception as e:
                print(f"Warning: Could not save JSON report to {json_output}: {e}")
        
        return report

    def export_unused_strings_to_csv(self, csv_file: str):
        """Export unused string keys to CSV format."""
        try:
            import csv
            
            with open(csv_file, 'w', newline='', encoding='utf-8') as f:
                writer = csv.writer(f)
                writer.writerow(['String Key', 'String Value', 'File Location'])
                
                for key in sorted(self.unused_keys):
                    value = self.all_strings.get(key, "")
                    # Find which file contains this string
                    file_location = "Unknown"
                    for string_file in self.string_files:
                        strings = self.parse_strings_xml(string_file)
                        if key in strings:
                            file_location = os.path.relpath(string_file, self.project_root)
                            break
                    
                    writer.writerow([key, value, file_location])
            
            print(f"📄 CSV export saved to: {csv_file}")
            
        except ImportError:
            print("Warning: CSV module not available, skipping CSV export")
        except Exception as e:
            print(f"Warning: Could not save CSV export to {csv_file}: {e}")


def main():
    parser = argparse.ArgumentParser(description='Advanced script to find unused string keys in an Android project')
    parser.add_argument('--project-root', default='.', help='Path to the project root directory')
    parser.add_argument('--output', help='Output file for the text report')
    parser.add_argument('--json', help='Output file for the JSON report')
    parser.add_argument('--csv', help='Output file for the CSV export of unused strings')
    parser.add_argument('--verbose', '-v', action='store_true', help='Enable verbose output')
    
    args = parser.parse_args()
    
    project_root = os.path.abspath(args.project_root)
    
    if not os.path.exists(project_root):
        print(f"Error: Project root directory '{project_root}' does not exist")
        return 1
    
    print(f"🚀 Analyzing Android project: {project_root}")
    print()
    
    try:
        analyzer = StringUsageAnalyzer(project_root, args.verbose)
        all_strings, used_keys, unused_keys = analyzer.analyze_strings()
        
        if args.verbose:
            print(f"📊 Analysis complete!")
            print(f"   - String resource files processed")
            print(f"   - Kotlin files scanned for usage")
            print()
        
        # Generate reports
        analyzer.generate_detailed_report(args.output, args.json)
        
        # Export to CSV if requested
        if args.csv:
            analyzer.export_unused_strings_to_csv(args.csv)
        
        if unused_keys:
            print(f"⚠️  Found {len(unused_keys)} unused string keys")
            return 1  # Exit with error code if unused keys found
        else:
            print("✅ All string keys are being used!")
            return 0
            
    except KeyboardInterrupt:
        print("\n❌ Analysis interrupted by user")
        return 1
    except Exception as e:
        print(f"❌ Error during analysis: {e}")
        if args.verbose:
            import traceback
            traceback.print_exc()
        return 1


if __name__ == "__main__":
    exit(main()) 