
; we need to handle 0 free page frames
;
; function
; argument 1 (page frame bitmap base address) should be page aligned
; return value in r4 (page frame index)
_frameAllocatorFirstFree:
  ENTER
  LOAD_I r15 _ramPhysicalSize
  LOAD_I r14 _ramPageSize
  DIVIDE r15 r14 r15   ; calc page frame count
  CR_TO_GR c3 r3
  LOAD_Y r14 r3  8     ; argument 1, base address of frame table
  MOVE_I r13 0h        ; frame index
_frameAllocatorFirstFreeLoop1:
  PUSH r13          ; push frame index
  PUSH r14          ; push frame table base address
  CALL_I _frameAllocatorCheck
                       ; return value is in r4
  CMP_Z r4
  JUMP_NZ_I _frameAllocatorFirstFreeLoop1End
  INCREMENT r13
  CMP r15 r13
  JUMP_NZ_I _frameAllocatorFirstFreeLoop1
_frameAllocatorFirstFreeLoop1End:
  MOVE r13 r4          ; put return value in r4
  
  LEAVE
  RETURN
.function _frameAllocatorFirstFree
.global   _frameAllocatorFirstFree
.size     _frameAllocatorFirstFree $ - _frameAllocatorFirstFree

.global _ramPhysicalSize
.global _ramPageSize
.global _frameAllocatorCheck
