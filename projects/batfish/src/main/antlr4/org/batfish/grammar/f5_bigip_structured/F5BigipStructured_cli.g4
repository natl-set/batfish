parser grammar F5BigipStructured_cli;

import F5BigipStructured_common;

options {
  tokenVocab = F5BigipStructuredLexer;
}

// CLI configuration - administrative settings we don't need
s_cli
:
  CLI ignored
;
