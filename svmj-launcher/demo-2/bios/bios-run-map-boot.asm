
_boot1BaseAddress:
  .4byte 4000h
.object _boot1BaseAddress
.global _boot1BaseAddress
.size _boot1BaseAddress $ - _boot1BaseAddress

_runMapBoot1:
  ENTER
  
  ; in the future we need to give the bios a way
  ; to search for devices on the memory address bus
  
  ; we cannot read the boot information table
  ; because it's address space is not mapped
  ; we will map a single page first at the start of the device
  ; and then we will do the dynamic mapping
  
  LOAD_I r5 _boot1BaseAddress
  LOAD_I r6 _ramPageSize
  PUSH r6
  PUSH r5
  PUSH r5
  CALL_I _mapPageRange
  
  LOAD_I r5 _boot1BaseAddress
  MOVE_I r8 0h                 ; boot external symbol index 0
  LOAD_4X r6 r5 r8             ; load index 0, image size in bytes page aligned
  PUSH r6
  PUSH r5
  PUSH r5
  CALL_I _mapPageRange

  LEAVE
  RETURN
.function _runMapBoot1
.global   _runMapBoot1
.size     _runMapBoot1 $ - _runMapBoot1

.global _ramPageSize
.global _mapPageRange