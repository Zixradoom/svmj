
; ** NOTE **
; to be able to use this subroutine when paging is enabled
; we must map a virtual address range to the plysical address
; range at the exact same location
; ** NOTE 2 **
; this is a privileged action and in the future will need to be
; protected so a lower privileged process cannot write to the
; page direcotries or tables
;
; this function will only work when paging is turned off
; or when the page directory is identity mapped
;
; function
; argument 1 (pd index)
; argument 2 (page table entry)
_writePageDirectoryEntry:
  ENTER
  CR_TO_GR c3 r3
  LOAD_Y r8 r3  8                   ; argument 1 (pd index)
  LOAD_Y r9 r3 12                   ; argument 2 (page table entry)
  CR_TO_GR c1 r1
  STORE_4X r9 r1 r8
  LEAVE
  RETURN
.function _writePageDirectoryEntry
.global   _writePageDirectoryEntry
.size     _writePageDirectoryEntry $ - _writePageDirectoryEntry