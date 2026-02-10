parser grammar Huawei_route_policy;

import Huawei_common;

options {
   tokenVocab = HuaweiLexer;
}

// Route-policy stanza
// route-policy <name> permit|deny node <node-id>
//   (if-match <condition>)*
//   (apply <action>)*
s_route_policy
:
   ROUTE_POLICY name = variable action = route_policy_action NODE node_id = uint16
   (
      route_policy_sub
   )*
;

// Route-policy action (permit or deny)
route_policy_action
:
   PERMIT
   | DENY
;

// Route-policy sub-statements (if-match and apply clauses)
route_policy_sub
:
   // Match conditions
   if_match_ip_prefix
   |
   if_match_community_filter
   |
   if_match_community
   |
   // Set actions
   apply_local_preference
   |
   apply_community
   |
   apply_cost
   |
   apply_preference
   |
   apply_tag
   |
   // Other sub-statements (consume but don't extract)
   null_rest_of_line
;

// if-match community-filter <number>
if_match_community_filter
:
   IF_MATCH COMMUNITY_FILTER filter_num = uint16
;

// if-match ip-prefix <prefix-list-name>
if_match_ip_prefix
:
   IF_MATCH IP_PREFIX prefix_list = variable
;

// if-match community <community-list>
if_match_community
:
   IF_MATCH COMMUNITY community_list += community_value+
;

// apply local-preference <value>
apply_local_preference
:
   APPLY LOCAL_PREFERENCE pref = uint32
;

// apply community <community-value>
apply_community
:
   APPLY COMMUNITY community_val += community_value+
;

// apply cost <value>
apply_cost
:
   APPLY COST cost = uint16
;

// apply preference <value>
apply_preference
:
   APPLY PREFERENCE preference = uint16
;

// apply tag <value>
apply_tag
:
   APPLY TAG tag = uint32
;
