
; run a user program, in the future this would be a boot loader
_run:
  ENTER

  CALL_I _runMapBoot1
  
  LOAD_I r5 _boot1BaseAddress
  MOVE_I r8 1h                 ; boot external symbol index 1
  LOAD_4X r6 r5 r8             ; load index 1, entry function
  
  CALL r6
  
  LEAVE
  RETURN
.function _run
.global   _run
.size     _run $ - _run

.global _runMapBoot1
.global _boot1BaseAddress