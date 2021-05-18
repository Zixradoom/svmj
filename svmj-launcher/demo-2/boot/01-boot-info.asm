
; these symbols have to start at '0'
; they are used by the bios to
; look up the functions and 
; data it requires to invoke us
_externalSymbolBase:
; size of image in bytes (must be page aligned)
; minium size 1 page
_extSize:
  .4byte 2000H
.object _extSize
.global _extSize
.size   _extSize $ - _extSize
; location of the entry function
_extEntry:
  .4byte 4048H
.object _extEntry
.global _extEntry
.size   _extEntry $ - _extEntry

.object _externalSymbolBase
.global _externalSymbolBase
.size   _externalSymbolBase $ - _externalSymbolBase