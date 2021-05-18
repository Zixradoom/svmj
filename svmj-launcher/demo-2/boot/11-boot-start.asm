
; c0 PROGRAM_COUNTER
; c1 PAGE_TABLE_BASE
; c2 STACK_POINTER
; c3 BASE_POINTER;
; c4 INTERRUPT_DESCRIPTOR_TABLE
; c5 PAGE_FAULT_ADDRESS
; c6 SYSTEM_ENTER_PROGRAM_COUNTER
; c7 SYSTEM_ENTER_STACK_POINTER

_bootStart:
  ENTER
  
  ; set c6 for system call
  MOVE_I r0 _systemFastCallEntry
  GR_TO_CR r0 c6
  
  ; set c7 for kernel stack
  CR_TO_GR c2 r2
  MOVE_I r3 400h
  SUBTRACT r2 r3 r2
  GR_TO_CR r2 c7

  CALL_I _systemCallStubTest
  
  ;MOVE_I r5 83h
  ;PUSH r5
  ;LOAD_I r4 _ramPhysicalBaseAddress
  ;PUSH r4
  ;CALL_I _biosFrameAllocatorMark
  
  LEAVE
  RETURN
.function _bootStart
.global   _bootStart
.size     _bootStart $ - _bootStart

.global _systemCallStubTest
.global _biosFrameAllocatorMark
.global _systemFastCallEntry