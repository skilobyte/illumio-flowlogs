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

6. Any line that does not conform to the valid log entry format will be logged to the console as an invalid log entry.

## Lookup Table File
1. The lookup table file contains no headers.

2. There are no duplicate or conflicting `dstport` and `protocol` combinations in the lookup file. If duplicates exist, the first match will be used.

3. If a unique `dstport` and `protocol` combination is mapped to two different tags, both tags are valid. For example:
```
80,tcp,tag1
80,tcp,tag2
```
Each matching log entry will affect both tag counts.

4. Empty lines are ignored.

5. Only the `tag` field is case-sensitive in the lookup table. For example:
These are treated as duplicate entries. However, `80,tcp,tag1` and `80,tcp,TAG1` are treated as unique entries.

6. Any `dstport` value out of range (0 to 65535) will be logged to the console as invalid.

7. Protocol names must follow the IANA shorthand naming convention (e.g., `tcp`, `udp`, etc.). Any protocol name not in the correct format will be ignored and logged to the console.

## Protocol Number Mapping
1. The mapping of protocol numbers to names follows the IANA protocol number resource.

2. Protocol names are defined using the shorthand convention listed on the IANA website [here](https://www.iana.org/assignments/protocol-numbers/protocol-numbers.xhtml).

3. If a protocol number does not have an entry in the IANA protocol number list, "unknown" will be used as the protocol name.