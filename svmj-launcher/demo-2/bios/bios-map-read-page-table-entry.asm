
; function
;
; this function requires a recursively mapped
; page directory entry at index 1023
;
; argument 1 (page directory index)
; argument 2 (page table index)
_mapReadPageEntry:
  ENTER
  CR_TO_GR c3 r3
  LOAD_Y r12 r3  8; argument 1
  LOAD_Y r13 r3 12; argument 2
  
  ; recursive look up
  MOVE_I r15 0h
  MOVE_I r16 3FFh
  MOVE_I r17 16h
  SHIFT_LEFT r16 r17
  OR r15 r16 r15
  
  ; or in the page directory index
  MOVE_I r17 ch
  SHIFT_LEFT r12 r17
  OR r15 r12 r15
  
  ; use base + index * 4 instruction to read entry
  LOAD_4X r4 r15 r13
  
  ; return value in r4
  
  LEAVE
  RETURN
.function _mapReadPageEntry
.global   _mapReadPageEntry
.size     _mapReadPageEntry $ - _mapReadPageEntry