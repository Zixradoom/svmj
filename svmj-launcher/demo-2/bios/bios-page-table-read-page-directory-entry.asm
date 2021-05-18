
; function
;
; this function will only work when paging is turned off
; or when the page directory is identity mapped
;
; argument 1 (pde index)
; return value in r4 (page directory entry)
_readPageDirectoryEntry:
  ENTER
  CR_TO_GR c3 r3
  CR_TO_GR c1 r1
  LOAD_Y r8 r3  8        ; argument 1 (page directory index)
  LOAD_4X r4 r1 r8       ; read page entry at index (r8) with base address (r1) (r4=[r1+r8*4])
  LEAVE
  RETURN
.function _readPageDirectoryEntry
.global   _readPageDirectoryEntry
.size     _readPageDirectoryEntry $ - _readPageDirectoryEntry