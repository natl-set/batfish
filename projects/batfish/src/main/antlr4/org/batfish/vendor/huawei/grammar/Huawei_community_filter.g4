parser grammar Huawei_community_filter;

import Huawei_common;

options {
   tokenVocab = HuaweiLexer;
}

// Community filter configuration
// Matches: ip community-filter <number> [permit|deny] <community-value>+

s_ip_community_filter
:
   IP COMMUNITY_FILTER filter_num = uint16
   (
      action = community_filter_action
   )
   communities += community_value+
;

// Community filter action: permit or deny
community_filter_action
:
   PERMIT
   | DENY
;
