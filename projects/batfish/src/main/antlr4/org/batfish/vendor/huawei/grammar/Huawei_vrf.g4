parser grammar Huawei_vrf;

import Huawei_common;

options {
   tokenVocab = HuaweiLexer;
}

// VRF configuration
// VPN-instance stanza (Huawei's term for VRF)
s_vrf
:
   IP VPN_INSTANCE vrf_name = variable
   (
      vrf_substanza
   )*
   (RETURN | QUIT | EXIT)?
;

// VRF sub-stanza
vrf_substanza
:
   vrf_route_distinguisher
   | vrf_vpn_target
   | vrf_description
   | vrf_address_family
   | vrf_null
;

// Route distinguisher configuration
vrf_route_distinguisher
:
   ROUTE_DISTINGUISHER rd = route_distinguisher_value
;

// Route distinguisher value (format: ASN:number or IP:number)
route_distinguisher_value
:
   // Use variable to match the RD value (e.g., 100:1, 65000:100, 1.2.3.4:100)
   // Validation happens in the extraction code
   variable
;

// VPN target (route target) configuration
vrf_vpn_target
:
   VPN_TARGET rt_value = route_target_value (rt_type = vpn_target_type)
;

// VPN target type (import, export, or both)
vpn_target_type
:
   IMPORT
   | EXPORT
   | BOTH
;

// Route target value (format: ASN:number or IP:number)
route_target_value
:
   // Use variable to match the RT value (e.g., 100:1, 65000:100, 1.2.3.4:100)
   // Validation happens in the extraction code
   variable
;

// VRF description
vrf_description
:
   description_line
;

// Address family configuration (ipv4-family or ipv6-family)
vrf_address_family
:
   (
     IPV4_FAMILY
     | IPV6_FAMILY
   )
   (
      vrf_af_substanza
   )*
   (RETURN | QUIT | EXIT)?
;

// Address family sub-stanza
vrf_af_substanza
:
   vrf_af_route_distinguisher
   | vrf_af_vpn_target
   | vrf_af_description
   | vrf_af_null
;

// Address family route distinguisher
vrf_af_route_distinguisher
:
   ROUTE_DISTINGUISHER rd = route_distinguisher_value
;

// Address family VPN target
vrf_af_vpn_target
:
   VPN_TARGET rt_value = route_target_value (rt_type = vpn_target_type)
;

// Address family description
vrf_af_description
:
   description_line
;

// Null VRF configuration (parse but ignore)
vrf_null
:
   NO?
   (
      null_rest_of_line
   )
;

// Null address family configuration (parse but ignore)
vrf_af_null
:
   NO?
   (
      null_rest_of_line
   )
;
