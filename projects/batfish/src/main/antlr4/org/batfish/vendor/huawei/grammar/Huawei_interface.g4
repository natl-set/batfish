parser grammar Huawei_interface;

import Huawei_common;

options {
   tokenVocab = HuaweiLexer;
}

// Interface configuration

// Main interface stanza
s_interface
:
   INTERFACE iname = interface_name
   (
      if_substanza
   )*
;

// Interface sub-stanzas
if_substanza
:
   if_description
   | if_ip_address
   | if_shutdown
   | if_dot1q_termination
   | if_ospf
   | if_null
;

// OSPF interface configuration
if_ospf
:
   OSPF
   (
      if_ospf_area
      | if_ospf_cost
      | if_ospf_network_type
      | if_ospf_timers
      | if_ospf_authentication
      | if_ospf_passive
   )
;

// OSPF area: ospf area <area-id>
if_ospf_area
:
   AREA area_id = uint32
;

// OSPF cost: ospf cost <value>
if_ospf_cost
:
   COST cost = uint32
;

// OSPF network type: ospf network-type {broadcast|p2p|p2mp|nbma}
if_ospf_network_type
:
   NETWORK_TYPE (BROADCAST | P2P | P2MP | NBMA)
;

// OSPF timers: ospf timer {hello|dead|retransmit-interval} <seconds>
if_ospf_timers
:
   TIMER
   (
      HELLO h = uint32
      |
      DEAD d = uint32
      |
      RETRANSMIT_INTERVAL r = uint32
   )
;

// OSPF authentication: ospf authentication-mode {md5|simple} <key>
if_ospf_authentication
:
   AUTHENTICATION_MODE (MD5 | SIMPLE) key = variable
;

// OSPF passive: ospf enable [passive] or ospf disable passive
if_ospf_passive
:
   (ENABLE | DISABLE) PASSIVE
;

// Null interface configuration (parse but ignore)
// Only matches commands that DON'T start with INTERFACE
// This prevents consuming subsequent interface statements
if_null
:
   NO?
   (
      // First token must not be INTERFACE
      // Match one token that's definitely not a stanza-starting keyword
      (DESCRIPTION | NAME | SHUTDOWN | DOT1Q | TERMINATION | VID | PORT | COMMAND | VARIABLE)
      // Then optionally match more tokens (including INTERFACE for descriptions)
      null_token*
   )
;

// Interface description
if_description
:
   description_line
;

// Interface IP address
if_ip_address
:
   IP ADDRESS
   (
      // ip address X.X.X.X Y.Y.Y.Y
      addr = IPV4_ADDRESS_PATTERN mask = IPV4_ADDRESS_PATTERN
   )
;

// Interface shutdown
if_shutdown
:
   (
      SHUTDOWN
      | UNDO SHUTDOWN
   )
;

// Subinterface dot1q termination (e.g., dot1q termination vid 100)
if_dot1q_termination
:
   DOT1Q TERMINATION VID vid = uint16
   (
      // Optional: dot1q termination vid <low> <high>
      low_vid = uint16
   )?
;
