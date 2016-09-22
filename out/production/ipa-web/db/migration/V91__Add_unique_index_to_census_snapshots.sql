ALTER TABLE CensusSnapshots ADD UNIQUE KEY `uk_section_id_snapshot_code` (SectionId, SnapshotCode);
