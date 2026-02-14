parser grammar Huawei_nat;

import Huawei_common;

options {
   tokenVocab = HuaweiLexer;
}

// NAT configuration

// NAT stanza - each NAT command is standalone
s_nat
:
   NAT
   (
      // NAT server - put FIRST because "server" keyword might be matched as variable otherwise
      NO? SERVER
      (
         GLOBAL ip_address INSIDE ip_address
         |
         PROTOCOL (TCP | UDP) GLOBAL ip_address global_port_proto = uint16 INSIDE ip_address inside_port_proto = uint16
         |
         GLOBAL ip_address global_port_simple = uint16 INSIDE ip_address
      )
      (
         VPN_INSTANCE VARIABLE
      )?
      |
      // NAT address-group
      // Format: nat address-group <index> [section 0 <start-ip> <end-ip>]
      // Or: nat address-group <index> <start-ip> <end-ip>
      // Or: nat address-group <index> address <ip> [mask <mask>]
      ADDRESS_GROUP group_index = uint16
      (
         SECTION uint8 ip_address ip_address
         |
         ADDRESS ip_address (MASK ip_address)?
         |
         ip_address ip_address
      )?
      |
      // NAT outbound
      NO? OUTBOUND
      (
         acl_num = uint16
         |
         acl_name = variable
      )
      (
         INTERFACE
         |
         POOL pool_name = variable
      )?
      (
         VPN_INSTANCE vrf_name = variable
      )?
      |
      // NAT static
      NO? STATIC
      (
         GLOBAL ip_address INSIDE ip_address
         |
         GLOBAL ip_address ip_address INSIDE ip_address ip_address
      )
      (
         VPN_INSTANCE VARIABLE
      )?
   )
;
