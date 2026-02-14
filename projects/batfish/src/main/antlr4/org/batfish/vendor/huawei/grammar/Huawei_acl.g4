parser grammar Huawei_acl;

import Huawei_common;

options {
   tokenVocab = HuaweiLexer;
}

// ACL configuration

// Unified ACL stanza - matches both IPv4 and IPv6 ACLs
// IPv6 ACLs use "acl ipv6 <name|number>" syntax
// IPv4 ACLs use "acl [number] <number|name> [basic|advanced]" syntax
s_acl
:
   ACL ACL_IPV6 acl_ipv6
   |
   ACL acl_ipv4
;

// IPv4 ACL stanza
acl_ipv4
:
   (
      // Handle "acl number <num> [basic|advanced]" syntax
      NUMBER acl_num_number = uint16 (acl_type = ACL_BASIC | acl_type = ACL_ADVANCED)?
      |
      // Handle "acl <num> [basic|advanced]" syntax
      acl_num = uint16 (acl_type = ACL_BASIC | acl_type = ACL_ADVANCED)?
      |
      // Handle "acl <name> [basic|advanced]" syntax
      acl_name = variable (acl_type = ACL_BASIC | acl_type = ACL_ADVANCED)?
   )
   (
      acl_substanza
   )*
;

// IPv6 ACL stanza
acl_ipv6
:
   (
      acl_name_ipv6 = variable
      |
      acl_num_ipv6 = uint16
   )
   (
      acl_ipv6_substanza
   )*
;

// ACL sub-stanza
acl_substanza
:
   acl_rule
   |
   acl_null
;

// ACL rule - permit/deny statements
acl_rule
:
   RULE uint16
   (
      action = PERMIT
      |
      action = DENY
   )
   (
      // Protocol specification
      TCP
      |
      UDP
      |
      ICMP
      |
      IP
      |
      variable
   )?
   (
      // Source address
      SOURCE src_addr = IPV4_ADDRESS_PATTERN (src_wildcard = IPV4_ADDRESS_PATTERN)?
      |
      SOURCE src_addr = IPV4_ADDRESS_PATTERN src_prefix_len = FORWARD_SLASH uint8
      |
      SOURCE src_any = ANY
   )?
   (
      // Destination address
      DESTINATION dest_addr = IPV4_ADDRESS_PATTERN (dest_wildcard = IPV4_ADDRESS_PATTERN)?
      |
      DESTINATION dest_addr = IPV4_ADDRESS_PATTERN dest_prefix_len = FORWARD_SLASH uint8
      |
      DESTINATION dest_any = ANY
   )?
   (
      // Source port (for TCP/UDP)
      SOURCE_PORT
      (
         eq = EQ src_port = uint16
         |
         gt = GT src_port = uint16
         |
         lt = LT src_port = uint16
         |
         range = RANGE src_port_start = uint16 src_port_end = uint16
      )
   )?
   (
      // Destination port (for TCP/UDP)
      DESTINATION_PORT
      (
         eq2 = EQ dest_port = uint16
         |
         gt2 = GT dest_port = uint16
         |
         lt2 = LT dest_port = uint16
         |
         range2 = RANGE dest_port_start = uint16 dest_port_end = uint16
      )
   )?
   (
      // Other options
      log = LOG
      |
      frag = FRAGMENT
   )?
;

// Null ACL configuration (parse but ignore)
acl_null
:
   NO?
   (
      null_rest_of_line
   )
;

// IPv6 ACL sub-stanza
acl_ipv6_substanza
:
   acl_ipv6_rule
   |
   acl_null
;

// IPv6 ACL rule - permit/deny statements for IPv6
acl_ipv6_rule
:
   RULE uint16
   (
      action = PERMIT
      |
      action = DENY
   )
   (
      // Protocol specification for IPv6
      TCP
      |
      UDP
      |
      ICMPV6
      |
      IPV6
      |
      variable
   )?
   (
      // IPv6 Source address - use IPV6_PREFIX for prefix notation
      SOURCE src_addr_ipv6 = IPV6_PREFIX
      |
      SOURCE src_addr_ipv6 = IPV6_ADDRESS
      |
      SOURCE src_any_ipv6 = ANY
   )?
   (
      // IPv6 Destination address - use IPV6_PREFIX for prefix notation
      DESTINATION dest_addr_ipv6 = IPV6_PREFIX
      |
      DESTINATION dest_addr_ipv6 = IPV6_ADDRESS
      |
      DESTINATION dest_any_ipv6 = ANY
   )?
   (
      // Source port (for TCP/UDP)
      SOURCE_PORT
      (
         eq = EQ src_port_ipv6 = uint16
         |
         gt = GT src_port_ipv6 = uint16
         |
         lt = LT src_port_ipv6 = uint16
         |
         range = RANGE src_port_start_ipv6 = uint16 src_port_end_ipv6 = uint16
      )
   )?
   (
      // Destination port (for TCP/UDP)
      DESTINATION_PORT
      (
         eq2 = EQ dest_port_ipv6 = uint16
         |
         gt2 = GT dest_port_ipv6 = uint16
         |
         lt2 = LT dest_port_ipv6 = uint16
         |
         range2 = RANGE dest_port_start_ipv6 = uint16 dest_port_end_ipv6 = uint16
      )
   )?
   (
      // Other options
      log = LOG
      |
      frag = FRAGMENT
   )?
;
