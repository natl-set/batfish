parser grammar CiscoNxos_crypto;

import CiscoNxos_common;

options {
  tokenVocab = CiscoNxosLexer;
}

ck_param
:
  PARAM RSA LABEL label = WORD MODULUS uint32 NEWLINE
;

crypto_key
:
  KEY
  (
    ck_param
  )
;

// CRYPTO token pushes M_NullLine mode, so we get NULL_LINE_TEXT for the rest of the line
s_crypto
:
  CRYPTO NULL_LINE_TEXT? NEWLINE
;