parser grammar F5BigipStructured_common;

options {
  tokenVocab = F5BigipStructuredLexer;
}

bracket_list
:
  BRACKET_LEFT
  (
    u_word
    | u_word_list
  )+ BRACKET_RIGHT
;

empty_list
:
  BRACE_LEFT BRACE_RIGHT
;

ip_address
:
  IP_ADDRESS
;

ip_address_port
:
  IP_ADDRESS_PORT
;

ip_prefix
:
  IP_PREFIX
;

ipv6_address
:
  IPV6_ADDRESS
;

ipv6_address_port
:
  IPV6_ADDRESS_PORT
;

ipv6_prefix
:
  IPV6_PREFIX
;

list
:
  empty_list
  | word_list
  | u_list
;

mac_address
:
  MAC_ADDRESS
;

structure_name
:
  partition = PARTITION? (word | word_id)
;

structure_name_or_address
:
  partition = PARTITION?
  (
    address = ip_address
    | address6 = ipv6_address
    | word_id
    | word
  )
;

structure_name_with_port
:
  partition = PARTITION?
  (
    ipp = ip_address_port
    | ip6p = ipv6_address_port
    | wp = word_port
  )
;

/*
 * An ignored fragment of syntax.
 * Must always be preceded by at least one token on line in which it appears.
 */
ignored
:
  u_word* list? NEWLINE
;

/*
 * Content inside profile/persist blocks that should be ignored.
 * Matches simple lines OR nested braced blocks.
 * Pattern: word { word* }
 *
 * NOTE: This is designed to be used iteratively (ignored_content+), where each
 * iteration matches one piece: a simple line, a block start, or a block end.
 */
ignored_content
:
  ignored                           // simple line
  | word BRACE_LEFT ~NEWLINE* NEWLINE                // nested block start: "method { ... }"
  | BRACE_RIGHT (~NEWLINE)* NEWLINE                  // nested block end: "} ..."
;

/*
 * An ignored multi-line braced block.
 * Used for metadata blocks where we don't need to parse the contents.
 * Generates NO warnings, unlike blocks using 'unrecognized'.
 *
 * Matches: BRACE_LEFT ... BRACE_RIGHT
 * Matches: token BRACE_LEFT ... BRACE_RIGHT (e.g., MANAGEMENT_IP 10.0.0.1 { })
 * Matches: name BRACE_LEFT ... BRACE_RIGHT (e.g., CERT /Common/name { })
 */
ignored_block
:
  (
    structure_name
    | ip_prefix
    | ip_address
    | word_id
    | word
  )? BRACE_LEFT
  (
    NEWLINE
    (
      ignored_line
      | ignored_inline_braced_content
    )*
  )? BRACE_RIGHT NEWLINE
;

ignored_line
:
  ~NEWLINE* NEWLINE
;

/*
 * A line that may contain nested braces on the same line.
 * Handles: metrics { events_count }
 * Handles: some-key { value } other-content
 */
ignored_inline_braced_content
:
  word BRACE_LEFT ~NEWLINE* BRACE_RIGHT ~NEWLINE* NEWLINE
;

/* An unrecognized fragment of syntax. When used, MUST be LAST alternative */
unrecognized
:
  last_word = u_word+ list? NEWLINE
;

u_list
:
  BRACE_LEFT NEWLINE unrecognized+ BRACE_RIGHT
;

u_word
:
  bracket_list
  | word
  | ACTIVE
  | ACTION
  | ADDRESS
  | ALL
  | ANY
  | APM
  | ASM
  | DEFAULT
  | DESCRIPTION
  | DESTINATION
  | DISABLED
  | DNS
  | ENABLED
  | FALSE
  | HOST
  | INFINITY
  | KEY
  | LTM
  | MODE
  | MODULE
  | NETWORK
  | NTP
  | PATH
  | PARTITION
  | PORT
  | PROTOCOL
  | SELF
  | SNMP
  | SOURCE
  | STATE
  | STATUS
  | SYS
  | GLOBAL_SETTINGS
  | TRUE
  | TYPE
  | VALUE
  | VERSION
;

u_word_list
:
  BRACE_LEFT NEWLINE? (u_word NEWLINE?)+ BRACE_RIGHT
;

uint16
:
  UINT16
  | VLAN_ID
;

uint32
:
  UINT16
  | UINT32
  | VLAN_ID
;

uint
:
  UINT16
  | UINT32
  | VLAN_ID
  | UNIT_ID
;

vlan_id
:
  VLAN_ID
;

word
:
  ~( BRACE_LEFT | BRACE_RIGHT | BRACKET_LEFT | BRACKET_RIGHT | IMISH_CHUNK |
  RULE_SPECIAL | NEWLINE )
;

word_id
:
  ~( BRACE_LEFT | BRACE_RIGHT | BRACKET_LEFT | BRACKET_RIGHT |
  DOUBLE_QUOTED_STRING | IMISH_CHUNK | NEWLINE | WORD )
;

word_port
:
  WORD_PORT
;

word_list
:
  BRACE_LEFT word+ BRACE_RIGHT
;
