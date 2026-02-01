parser grammar Huawei_ospf;

import Huawei_common;

options {
   tokenVocab = HuaweiLexer;
}

// OSPF configuration

// OSPF stanza: ospf <process-id>
s_ospf
:
   OSPF process_id = uint32
   (
      ospf_substanza
   )*
;

// OSPF sub-stanza
ospf_substanza
:
   ospf_area
   | ospf_network
   | ospf_router_id
   | ospf_default_originate
   | ospf_default
   | ospf_virtual_link
   | ospf_import_route
   | ospf_null
;

// OSPF area configuration
ospf_area
:
   AREA area_id = uint32
;

// Area sub-stanza
area_substanza
:
   area_stub
   | area_nssa
   | area_authentication
   | area_null
;

// Stub area: area <id> stub [no-summary]
area_stub
:
   STUB (no_summary = NO_SUMMARY)?
;

// NSSA area: area <id> nssa [no-summary] [no-redistribute] [default-information-originate]
area_nssa
:
   NSSA (NO_SUMMARY)? (NO_REDISTRIBUTE)? (DEFAULT_INFORMATION_ORIGINATE)?
;

// Area authentication
area_authentication
:
   AUTHENTICATION_MODE (MD5 key = variable | SIMPLE key = variable)
;

// OSPF network statement: network <prefix> area <area-id>
ospf_network
:
   NETWORK ip = ip_prefix AREA area_id = uint32
;

// OSPF router-id: router-id A.B.C.D
ospf_router_id
:
   ROUTER_ID router_ip = ip_address
;

// OSPF default-information originate: default-information originate [always]
ospf_default_originate
:
   DEFAULT_INFORMATION_ORIGINATE (route_map = VARIABLE)?
;

// OSPF default: default { cost <value> | tag <value> | type {1 | 2} }
ospf_default
:
   DEFAULT
   (
      ospf_default_cost
      | ospf_default_tag
      | ospf_default_type
   )+
;

// OSPF default cost clause
ospf_default_cost
:
   COST cost = uint32
;

// OSPF default tag clause
ospf_default_tag
:
   TAG tag = uint32
;

// OSPF default type clause
ospf_default_type
:
   TYPE type_value = ospf_type_value
;

// Helper rule for OSPF type values (1 or 2)
ospf_type_value
:
   uint8
   | uint16
;

// OSPF virtual-link: vlink-peer <router-id> [hello <seconds>] [dead <seconds>]
ospf_virtual_link
:
   VIRTUAL_LINK router_id = ip_address (HELLO_INTERVAL h = uint32)? (DEAD_INTERVAL d = uint32)?
;

// OSPF import-route (redistribution): import-route [direct|static|ospf|bgp|rip|isis|unr] [cost <value>] [type <value>] [tag <value>] [route-policy <policy>]
ospf_import_route
:
   IMPORT_ROUTE
   (
      protocol = DIRECT
      | protocol = STATIC
      | protocol = OSPF
      | protocol = BGP
      | protocol = RIP
      | protocol = ISIS
      | protocol = UNR
   )
   (
      ospf_import_cost
      | ospf_import_type
      | ospf_import_tag
      | ospf_import_route_policy
   )*
;

// OSPF import-route cost clause
ospf_import_cost
:
   COST cost = uint32
;

// OSPF import-route type clause
ospf_import_type
:
   TYPE type_value = ospf_type_value
;

// OSPF import-route tag clause
ospf_import_tag
:
   TAG tag = uint32
;

// OSPF import-route route-policy clause
ospf_import_route_policy
:
   ROUTE_POLICY route_policy = VARIABLE
;

// Null area configuration (parse but ignore)
area_null
:
   NO?
   (
      null_rest_of_line
   )
;

// Null OSPF configuration (parse but ignore)
ospf_null
:
   NO?
   (
      null_rest_of_line
   )
;
