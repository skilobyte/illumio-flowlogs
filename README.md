# illumio-flowlogs

## Flow Log File
1. Each flow log entry strictly adheres to the specified format with space as the delimiter:
`version account-id interface-id srcaddr dstaddr srcport dstport protocol packets bytes start end action log-status`

2. The flow log file contains no headers.

3. A valid log entry must follow the four rules:
- Has exactly 14 fields.
- Starts with `2` (indicating version 2) and ends with one of the three valid log statuses: `ok`, `skipdata`, or `nodata`.
- The `dstport` field must contain port numbers in the range 0 to 65535.
- The `protocol` field must be a valid protocol number between 0 and 255.

4. Only the `dstport` and `protocol` fields are checked for validity. Other fields can contain invalid data (e.g., `srcaddr` may contain an invalid IP like `999.999.999.999`), and such entries will still be treated as valid log entries.

5. Empty lines are ignored.

6. Any line that does not conform to the valid log entry format will be ignored and logged to the console as an invalid log entry.

## Lookup Table File
1. The lookup table file is in .txt format and contains no headers.

2. If a unique `dstport` and `protocol` combination is mapped to two different tags, both tags are valid. For example:
```
80,tcp,tag1
80,tcp,tag2
```
Each matching log entry will affect both tag counts.

4. The valid range for ports is 0 to 65535. Any entry with an out-of-range `dstport` value is ignored and logged to the console as an invalid lookup table entry.

5. Protocol names in the lookup table file must follow the IANA shorthand naming convention (e.g., `tcp`, `udp`, etc.). If they do not match to any IANA protocol names, the log table entry is ignored and logged to console as invalid look table entry.

6. Empty lines are ignored.

7. Only the `tag` field is case-sensitive in the lookup table. For example:
```
Example-1
80,tcp,tag1
80,TCP,tag1
```
```
Example-2
80,tcp,tag1
80,tcp,TAG1
```
Entries in Example-1 are treated as duplicate entries. However, entries in Example-2 are treated as unique entries.

## Protocol Number Mapping
1. The mapping of protocol numbers to names follows the IANA protocol number resource.

2. Protocol names are defined using the shorthand convention listed on the IANA website [here](https://www.iana.org/assignments/protocol-numbers/protocol-numbers.xhtml).

3. "unknown" will be used as the protocol name if the protocol number does not have a corresponding protocol name listed on the IANA website.
