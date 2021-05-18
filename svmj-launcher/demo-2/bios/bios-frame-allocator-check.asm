
; function
; argument 1, base address of frame table
; argument 2, index of frame
; return value in r4 (page frame index status) 0: False, 1: True
_frameAllocatorCheck:
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
  AND r9 r5 r12       ; extract the bit for the index
  CMP_Z r12           ; compare to zero
  JUMP_NZ_I _frameAllocatorCheckReturnFalse
                      ; jump forword pc relative 2 instruction
  MOVE_I r4 1h        ; return true
  JUMP_I _frameAllocatorCheckEnd
                      ; jump forword pc relative 1 instruction
_frameAllocatorCheckReturnFalse:
  MOVE_I r4 0h        ; return false
_frameAllocatorCheckEnd:
  LEAVE
  RETURN
.function _frameAllocatorCheck
.global   _frameAllocatorCheck
.size     _frameAllocatorCheck $ - _frameAllocatorCheck
