parser grammar F5BigipStructured_analytics;

import F5BigipStructured_common;

options {
  tokenVocab = F5BigipStructuredLexer;
}

s_analytics
:
  ANALYTICS a_gui_widget
;

a_gui_widget
:
  GUI_WIDGET ignored_block
;
