parser grammar F5BigipStructured_security;

import
  F5BigipStructured_common;

options {
  tokenVocab = F5BigipStructuredLexer;
}

s_security
:
  // Nested format: security { firewall { rule-list ... } }
  SECURITY BRACE_LEFT
  (
    NEWLINE
    (
      sec_firewall
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

// Flat format support: security firewall rule-list X { ... }
s_security_firewall_rule_list
:
  SECURITY FIREWALL RULE_LIST name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      secsfrl_rules
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

// Rules block containing individual rules (flat format)
secsfrl_rules
:
  RULES BRACE_LEFT
  (
    NEWLINE
    (
      secsfrlr_individual_rule
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

// Individual rule inside rules block (flat format)
secsfrlr_individual_rule
:
  name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      secsfrlri_action
      | secsfrlri_ip_protocol
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

// Action for individual rule (flat format)
secsfrlri_action
:
  ACTION
  (
    ACCEPT
    | DROP
    | REJECT
  ) NEWLINE
;

// IP protocol for individual rule (flat format)
secsfrlri_ip_protocol
:
  IP_PROTOCOL word NEWLINE
;

sec_firewall
:
  FIREWALL BRACE_LEFT
  (
    NEWLINE
    (
      secf_rule_list
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

secf_rule_list
:
  RULE_LIST name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      secfrl_rule
      | secfrl_rules
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

// Rules block in nested format
secfrl_rules
:
  RULES BRACE_LEFT
  (
    NEWLINE
    (
      secfrlr_individual_rule
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

// Individual rule in nested format
secfrlr_individual_rule
:
  name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      secfrlri_action
      | secfrlri_ip_protocol
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

// Action for individual rule (nested format) - reuse flat format rules
secfrlri_action
:
  ACTION
  (
    ACCEPT
    | DROP
    | REJECT
  ) NEWLINE
;

// IP protocol for individual rule (nested format) - reuse flat format rules
secfrlri_ip_protocol
:
  IP_PROTOCOL word NEWLINE
;

secfrl_rule
:
  RULE name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      secfrlr_action
      | secfrlr_ip_protocol
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

secfrlr_action
:
  ACTION
  (
    ACCEPT
    | DROP
    | REJECT
  ) NEWLINE
;

secfrlr_ip_protocol
:
  IP_PROTOCOL word NEWLINE
;
