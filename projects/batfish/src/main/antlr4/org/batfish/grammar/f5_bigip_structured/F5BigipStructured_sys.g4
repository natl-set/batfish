parser grammar F5BigipStructured_sys;

import F5BigipStructured_common;

options {
  tokenVocab = F5BigipStructuredLexer;
}

sgs_hostname
:
  HOSTNAME hostname = word NEWLINE
;

sgs_null
:
  (
    CONSOLE_INACTIVITY_TIMEOUT
    | GUI_SECURITY_BANNER_TEXT
    | GUI_SETUP
  ) ignored
;

sys_dns
:
  DNS ignored_block
;

sys_global_settings
:
  GLOBAL_SETTINGS BRACE_LEFT
  (
    NEWLINE
    (
      sgs_hostname
      | sgs_null
      | ignored
    )*
  )? BRACE_RIGHT NEWLINE
;

sys_ha_group
:
  HA_GROUP name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      sh_active_bonus
      | sh_pools
      | sh_trunks
      | ignored
    )*
  )? BRACE_RIGHT NEWLINE
;

sh_active_bonus
:
  ACTIVE_BONUS bonus = uint16 NEWLINE
;

sh_pools
:
  POOLS BRACE_LEFT
  (
    NEWLINE shp_pool*
  )? BRACE_RIGHT NEWLINE
;

shp_pool
:
  name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      shpp_weight
      | ignored
    )*
  )? BRACE_RIGHT NEWLINE
;

shpp_weight
:
  WEIGHT weight = uint16 NEWLINE
;

sh_trunks
:
  TRUNKS BRACE_LEFT
  (
    NEWLINE sht_trunk*
  )? BRACE_RIGHT NEWLINE
;

sht_trunk
:
  name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      shtt_weight
      | ignored
    )*
  )? BRACE_RIGHT NEWLINE
;

shtt_weight
:
  WEIGHT weight = uint16 NEWLINE
;

sys_management_ip
:
  MANAGEMENT_IP ignored_block
;

sys_management_route
:
  MANAGEMENT_ROUTE ignored_block
;

sys_ntp
:
  NTP BRACE_LEFT
  (
    NEWLINE
    (
      ntp_null
      | ntp_servers
      | ignored
    )*
  )? BRACE_RIGHT NEWLINE
;

ntp_null
:
  TIMEZONE ignored
;

ntp_servers
:
  SERVERS BRACE_LEFT servers += word* BRACE_RIGHT NEWLINE
;

sys_null
:
  (
    DYNAD
    | FEATURE_MODULE
    | FOLDER
    | FPGA
    | HTTPD
    | MANAGEMENT_DHCP
    | OUTBOUND_SMTP
    | PROVISION
    | SFLOW
    | TURBOFLEX
  ) ignored
;

sys_sshd
:
  SSHD ignored_block
;

sys_log_config
:
  LOG_CONFIG DESTINATION word structure_name BRACE_LEFT
  (
    NEWLINE
    (
      ignored_content
    )*
  )? BRACE_RIGHT NEWLINE
;

sys_syslog
:
  SYSLOG ignored_block
;

sys_snmp
:
  SNMP BRACE_LEFT
  (
    NEWLINE
    (
      snmp_agent_addresses
      | snmp_allowed_addresses
      | snmp_communities
      | snmp_disk_monitors
      | snmp_process_monitors
      | snmp_sys_contact_null
      | snmp_sys_location_null
      | snmp_traps
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE?
;

snmp_agent_addresses
:
  AGENT_ADDRESSES BRACE_LEFT addresses += word* BRACE_RIGHT NEWLINE
;

snmp_allowed_addresses
:
  ALLOWED_ADDRESSES BRACE_LEFT addresses += word* BRACE_RIGHT NEWLINE
;

snmp_communities
:
  COMMUNITIES BRACE_LEFT NEWLINE
  (
    snmp_community
  )* BRACE_RIGHT NEWLINE
;

snmp_community
:
  name = structure_name BRACE_LEFT NEWLINE
  (
    snmp_community_name
    | snmp_community_source
  )*
  BRACE_RIGHT NEWLINE
;

snmp_community_name
:
  COMMUNITY_NAME name = word_id NEWLINE
;

snmp_community_source
:
  SOURCE source = word NEWLINE
;

snmp_disk_monitors
:
  DISK_MONITORS BRACE_LEFT
  snmp_disk_monitors_content*
  BRACE_RIGHT NEWLINE
;

snmp_disk_monitors_content
:
  NEWLINE snmp_disk_monitor?
;

snmp_disk_monitor
:
  name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      snmp_minspace_null
      | snmp_disk_path_null
      | ignored
    )*
  )? BRACE_RIGHT
;

snmp_disk_path_value
:
  (PARTITION | word)+
;

snmp_minspace_null
:
  MINSPACE space = uint NEWLINE
;

snmp_disk_path_null
:
  PATH path = snmp_disk_path_value NEWLINE
;

snmp_process_monitors
:
  PROCESS_MONITORS BRACE_LEFT
  snmp_process_monitors_content*
  BRACE_RIGHT NEWLINE
;

snmp_process_monitors_content
:
  NEWLINE snmp_process_monitor?
;

snmp_process_monitor
:
  name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      snmp_max_processes_null
      | snmp_process_name_null
      | ignored
    )*
  )? BRACE_RIGHT
;

snmp_max_processes_null
:
  MAX_PROCESSES (INFINITY | uint) NEWLINE
;

snmp_process_name_null
:
  PROCESS name = word NEWLINE
;

snmp_sys_contact_null
:
  SYS_CONTACT contact = word NEWLINE
;

snmp_sys_location_null
:
  SYS_LOCATION location = word NEWLINE
;

snmp_traps
:
  TRAPS BRACE_LEFT
  (
    NEWLINE
    (
      snmp_trap
    )*
  )? BRACE_RIGHT NEWLINE
;

snmp_trap
:
  name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      snmp_trap_community
      | snmp_trap_host
      | ignored
    )*
  )? BRACE_RIGHT NEWLINE
;

snmp_trap_community
:
  COMMUNITY community = word_id NEWLINE
;

snmp_trap_host
:
  HOST host = ip_address NEWLINE
;

s_sys
:
  SYS
  (
    sys_compatibility_level
    | sys_diags_ihealth
    | sys_ecm
    | sys_log_config
    | sys_management_ovsdb
    | sys_dns
    | sys_global_settings
    | sys_ha_group
    | sys_httpd
    | sys_management_ip
    | sys_management_route
    | sys_ntp
    | sys_null
    | sys_outbound_smtp
    | sys_provision
    | sys_snmp
    | sys_software_update
    | sys_sshd
    | sys_syslog
    | sys_wom_deduplication
    | unrecognized
  )
;

sys_ecm
:
  ECM CLOUD_PROVIDER ignored_block
;

sys_software_update
:
  SOFTWARE UPDATE ignored_block
;

sys_wom_deduplication
:
  WOM DEDUPLICATION ignored_block
;

sys_httpd
:
  HTTPD ignored_block
;

sys_outbound_smtp
:
  OUTBOUND_SMTP ignored_block
;

sys_provision
:
  PROVISION structure_name ignored_block
;

sys_diags_ihealth
:
  DIAGS IHEALTH ignored_block
;

sys_management_ovsdb
:
  MANAGEMENT_OVSDB ignored_block
;

sys_compatibility_level
:
  COMPATIBILITY_LEVEL ignored_block
;