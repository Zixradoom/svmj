
_TestDat1:
  .4byte 10B0h
  .4byte 10B1h
  .4byte 10B2h
  .4byte 10B3h
.object _TestDat1
.size _TestDat1 $ - _TestDat1

_start:
  ADD r4 r5 r6
  CALL_I _func1
  HALT
  
_func1:
  ENTER
  PUSH r0
  LEAVE
  RETURN
_func1End:
.function _func1
.global _func1
.size _func1 _func1End - _func1
