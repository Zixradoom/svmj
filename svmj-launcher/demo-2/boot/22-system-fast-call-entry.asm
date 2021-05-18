
_systemFastCallEntry:
  ENTER
  ; r0 contains the return address for SYSTEM_EXIT
  ; r1 contains the stack address for ring 3
  ; r4 contains system call number
  
  ; save the return values for later
  ; on the kernel stack
  PUSH r0
  PUSH r1
  
  ; load the address of the
  ; system call out of the
  ; system call vector table
  ; stored in r23
  LOAD_4X r0 r23 r4
  ;CALL r0
  
  ; return value is r4 if there was one
  ; do not change r4
  
  ; pop the ring 3 parameters from the stack
  POP r1
  POP r0
  
  ; do this first while the TLB is still primed
  LEAVE
  ; flush the tlb to protect the kernel
  TLB_FLUSH
  ; return to the ring 3 code
  SYSTEM_EXIT
.function _systemFastCallEntry
.global   _systemFastCallEntry
.size     _systemFastCallEntry $ - _systemFastCallEntry