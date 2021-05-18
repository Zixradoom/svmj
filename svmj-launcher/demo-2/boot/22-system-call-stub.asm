
; c0 PROGRAM_COUNTER
; c1 PAGE_TABLE_BASE
; c2 STACK_POINTER
; c3 BASE_POINTER;
; c4 INTERRUPT_DESCRIPTOR_TABLE
; c5 PAGE_FAULT_ADDRESS
; c6 SYSTEM_ENTER_PROGRAM_COUNTER
; c7 SYSTEM_ENTER_STACK_POINTER

; the instructions SYSTEM_ENTER and SYSTEM_EXIT
; form an entry into the system kernal functions
; there are no explicit arguments for these
; instructions, they use implicit control registers

_systemCallStub:
  ENTER
  CR_TO_GR c3 r3
  
  ; we need to save the return address
  ; and the stack pointer for the fast call
  ; entry function to restore our state
  
  LOAD_Y r4 r3  8; argument 1 (kernel function to call)
  
  ; save the stack pointer to r1
  CR_TO_GR c2 r1
  ; save the return address to r0
  MOVE_I r0 _systemCallStubReturn
  
  SYSTEM_ENTER
  
  ; when the system returns to this point
  ; our base pointer will be incorrect but,
  ; our stack pointer should be restored
  ; we did not push anything on the stack
  ; so we do not need to clean it
  ; when leave is executed our base pointer
  ; should be fixed
_systemCallStubReturn:
  
  LEAVE
  RETURN
.function _systemCallStub
.global   _systemCallStub
.size     _systemCallStub $ - _systemCallStub

.global _systemCallStubReturn