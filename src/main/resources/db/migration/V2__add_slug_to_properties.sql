ALTER TABLE properties
ADD COLUMN slug VARCHAR(255);

CREATE UNIQUE INDEX uq_properties_slug ON properties(slug);

ALTER TABLE agents
ADD COLUMN slug VARCHAR(255);

CREATE UNIQUE INDEX uq_agents_slug ON agents(slug);