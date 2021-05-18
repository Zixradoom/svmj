
; function
;
; this function will only work when paging is turned off
; or when the page directory is identity mapped
;
; argument 1 (pde index)
_writePageTableEntry:
  ENTER
  CR_TO_GR c3 r3
  LOAD_Y r8 r3 8                    ; argument 1 (pde index)
  PUSH r8
  CALL_I _readPageDirectoryEntry
  
  ; return value is in r4
  LOAD_I r5 _pageDirectoryEntryAddressMask
  AND r4 r5 r6

  CR_TO_GR c3 r3
  LOAD_Y  r9 r3  12                 ; argument 2 (pte index)
  LOAD_Y r10 r3  16                 ; argument 3 (pte)
  MOVE_I r5 1h
  STORE_4X r10 r6 r9                ; r10 value to store r6 base address r9 index ([r6+r9*4]=r10)

  LEAVE
  RETURN
.function _writePageTableEntry
.global   _writePageTableEntry
.size     _writePageTableEntry $ - _writePageTableEntry

.global _readPageDirectoryEntry
.global _pageDirectoryEntryAddressMask