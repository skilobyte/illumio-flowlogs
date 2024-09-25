# illumio-flowlogs

## Instructions to run the program

1. Navigate to the project directory: `cd illumio-flowlogs`

2. Compile the program using javac: `javac flowLogMetrics.java`

3. Run the program with required parameters: `java flowLogMetrics flowlog-filename lookup-filename`

- flowlog-filename: The file containing the flow logs. (e.g., `files/flow_logs.txt`)
- lookup-filename: The lookup table file for mapping destination ports and protocols to tags. (e.g., `files/lookup_table.txt`)

## Output 
- The tag counts will be saved in: `output/tag.csv`
- The counts of each port/protocol combination will be saved in: `output/port-protocol.csv`
- Any invalid input lines encountered during processing will be recorded in: `output/error_log.txt`

## Assumptions
### Protocol Number to Protocol Name translation
1. Protocol names are defined using the protocol keywords listed on the [IANA website ](https://www.iana.org/assignments/protocol-numbers/protocol-numbers.xhtml).

2. `"unknown"` will be used as the protocol name if the protocol number does not have a corresponding protocol keyword listed on the IANA website.

### Flow Log File
1. The flow log file is in `.txt` format and contains no headers.

2. Each flow log record strictly adheres to the following specified format with a single space as the delimiter:
`version account-id interface-id srcaddr dstaddr srcport dstport protocol packets bytes start end action log-status`

3. A valid log record must follow the four rules:
- Has exactly 14 fields.
- Starts with `2` (indicating version 2) and ends with one of the three valid log-status values: `ok`, `skipdata`, or `nodata`.
- The `dstport` field must contain port numbers in the range 0 to 65535.
- The `protocol` field must be a valid protocol number between 0 and 255.

4. Only the `dstport` and `protocol` fields are checked for validity. Other fields can contain invalid data (e.g., `srcaddr` may contain an invalid IP like `999.999.999.999`), and such records will still be treated as valid log records. 

5. Any line that does not conform to the valid log record format will be ignored and logged to the `error_log.txt` file as an invalid log record. (Empty lines are ignored)

### Lookup Table File
1. The lookup table file is in `.txt` format and contains no headers. The records are strictly in `port,protcol,tag` with `,` as the delimiter.

2. If a unique `dstport` and `protocol` combination is mapped to two different tags, both tags are valid. For example:
```
80,tcp,tag1
80,tcp,tag2
```
Each matching log record will affect both tag counts.

4. The valid range for ports is 0 to 65535. Any record with an out-of-range `dstport` value is ignored and logged to the `error_log.txt` file as an invalid lookup table record.

5. Protocol names in the lookup table file must be same as the IANA protocol keywords (e.g., `tcp`, `udp`, etc.). If they do not match to any IANA protocol keywords, the lookup table record is ignored and logged to `error_log.txt` file as invalid look up table record.

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
Entries in Example-1 are treated as duplicate records. However, entries in Example-2 are treated as unique records.
