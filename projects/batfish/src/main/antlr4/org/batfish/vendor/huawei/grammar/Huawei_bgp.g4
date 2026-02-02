parser grammar Huawei_bgp;

import Huawei_common;

options {
   tokenVocab = HuaweiLexer;
}

// BGP configuration

// BGP stanza
s_bgp
:
   BGP as_num = uint32
   (
      bgp_substanza
   )*
;

// BGP sub-stanzas
bgp_substanza
:
   bgp_router_id
   | bgp_peer
   | bgp_peer_group
   | bgp_network
   | bgp_import
   | bgp_export
   | bgp_address_family
   | bgp_null
;

// Router ID: router-id 1.1.1.1
bgp_router_id
:
   ROUTER_ID router_ip = ip_address
;

// BGP peer: peer 192.168.1.2 as-number 65002
// Note: as-number is optional for edge case handling (malformed configs)
bgp_peer
:
   PEER peer_ip = ip_address (AS_NUMBER peer_as = uint16)?
   (
      // Optional peer parameters
      bgp_peer_param
   )*
;

// BGP peer parameters
bgp_peer_param
:
   // peer X.X.X.X connect-interface GigabitEthernet0/0/0
   CONNECT_INTERFACE iface = variable
   |
   // peer X.X.X.X password <password>
   PASSWORD password = variable
   |
   // peer X.X.X.X group <group-name>
   GROUP group_name = variable
   |
   // Other parameters (ignore for now)
   null_rest_of_line
;

// BGP peer group: group GROUP_NAME [internal|external]
bgp_peer_group
:
   GROUP group_name = variable
   (
      // Optional group parameters
      bgp_group_param
   )*
;

// BGP peer group parameters
bgp_group_param
:
   // Type: internal or external
   INTERNAL
   |
   EXTERNAL
   |
   // Remote AS: as-number 65002
   AS_NUMBER as_num = uint32
   |
   // Password: password <password>
   PASSWORD password = variable
   |
   // Route policy: route-policy <name> [import|export]
   ROUTE_POLICY policy = variable (IMPORT | EXPORT)?
   |
   // Route reflector client: route-reflector-client [cluster-id <id>]
   ROUTE_REFLECTOR_CLIENT (CLUSTER_ID id = ip_address)?
   |
   // Other parameters (ignore for now)
   null_rest_of_line
;

// Network announcement: network 10.0.0.0 255.255.255.0 [route-policy <name>]
bgp_network
:
   NETWORK network_addr = ip_address network_mask = ip_address (ROUTE_POLICY policy = variable)?
;

// Import policy: import-route <protocol> [route-policy <name>]
bgp_import
:
   IMPORT_ROUTE (DIRECT | STATIC | RIP | RIPNG | OSPF | ISIS | protocol = variable) (ROUTE_POLICY policy = variable)?
   |
   null_rest_of_line
;

// Export policy
bgp_export
:
   null_rest_of_line
;

// Address family configuration
bgp_address_family
:
   (IPV4 | IPV6) (FAMILY)?
   (
      UNICAST
      |
      MULTICAST
      |
      VPN
   )*
   (
      // Address family sub-configuration
      bgp_af_substanza
   )*
;

// Address family sub-stanzas
bgp_af_substanza
:
   null_rest_of_line
;

// Null BGP configuration (parse but ignore)
bgp_null
:
   NO?
   (
      null_rest_of_line
   )
;
