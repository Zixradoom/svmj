
; function
; argument 1, base address of frame table
; argument 2, index of frame
_frameAllocatorMark:
  ENTER
  CR_TO_GR c3 r3
  LOAD_Y r8 r3  8     ; argument 1, base address of frame table
  LOAD_Y r9 r3 12     ; argument 2, index of frame
  MOVE_I r4 20h       ; element size is 32 bits 
  DIVIDE r9 r4 r10    ; divide index by element size in bits
  MOD r9 r4 r11       ; mod index by element size in bits
  MOVE_I r5 1h
  SHIFT_LEFT r5 r11   ; calc mask for bit
  LOAD_4X r9 r8 r10   ; read current element value
  OR r9 r5 r12        ; mark the bit for the index
  STORE_4X r12 r8 r10  ; write back the element
  LEAVE
  RETURN
.function _frameAllocatorMark
.global   _frameAllocatorMark
.size     _frameAllocatorMark $ - _frameAllocatorMark