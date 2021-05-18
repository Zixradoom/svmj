
_start:
  ; fixup stack as it is currently incorrect
  LOAD_I r4 _ramPhysicalBaseAddress
  
  ; the vm starts with the base pointer set to the physical size of ram
  CR_TO_GR c3 r5
  ADD r4 r5 r5  
  GR_TO_CR r5 c3
  
  ; the vm starts with the stack pointer set to the physical size of ram
  CR_TO_GR c2 r5
  ADD r4 r5 r5
  GR_TO_CR r5 c2
  
  CALL_I _init
  
  ; when init returns we are running in paged mode
  
  CALL_I _run
  
  HALT
.global _start
.size   _start $ - _start

.global _ramPhysicalBaseAddress
.global _init
.global _run