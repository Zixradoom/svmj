

_ramPhysicalBaseAddress: 
  .4byte 1F000000H
.global _ramPhysicalBaseAddress .object _ramPhysicalBaseAddress .size _ramPhysicalBaseAddress $ - _ramPhysicalBaseAddress

_ramPhysicalSize:
  .4byte 200000h
.global _ramPhysicalSize .object _ramPhysicalSize .size _ramPhysicalSize $ - _ramPhysicalSize

_ramPageSize:
  .4byte 1000h
.global _ramPageSize .object _ramPageSize .size _ramPageSize $ - _ramPageSize

_pageDirectoryEntrySize:
  .4byte 4h
.global _pageDirectoryEntrySize .object _pageDirectoryEntrySize .size _pageDirectoryEntrySize $ - _pageDirectoryEntrySize

_pageDirectoryEntryAddressMask:
  .4byte FFFFF000h
.global _pageDirectoryEntryAddressMask .object _pageDirectoryEntryAddressMask .size _pageDirectoryEntryAddressMask $ - _pageDirectoryEntryAddressMask

_pageDirectoryIndexMask:
  .4byte FFC00000h
.global _pageDirectoryIndexMask .object _pageDirectoryIndexMask .size _pageDirectoryIndexMask $ - _pageDirectoryIndexMask

_pageTableEntrySize:
  .4byte 4h
.global _pageTableEntrySize .object _pageTableEntrySize .size _pageTableEntrySize $ - _pageTableEntrySize

_pageTableEntryAddressMask:
  .4byte FFFFF000h
.global _pageTableEntryAddressMask .object _pageTableEntryAddressMask .size _pageTableEntryAddressMask $ - _pageTableEntryAddressMask

_pageTableIndexMask:
  .4byte 3FF000h
.global _pageTableIndexMask .object _pageTableIndexMask .size _pageTableIndexMask $ - _pageTableIndexMask
