import os
import re

base_dir = r"D:\work\multitrack\src\main\java\org\nashinnov8\multitrack"

def replace_in_file(filepath, replacements):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    for old, new in replacements:
        content = content.replace(old, new)
        
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

# 1. common
replace_in_file(os.path.join(base_dir, 'common', 'domain', 'BaseEntity.java'), [
    ('package org.nashinnov8.multitrack.domain;', 'package org.nashinnov8.multitrack.common.domain;')
])

# 2. user
replace_in_file(os.path.join(base_dir, 'user', 'domain', 'User.java'), [
    ('package org.nashinnov8.multitrack.domain;', 'package org.nashinnov8.multitrack.user.domain;\n\nimport org.nashinnov8.multitrack.common.domain.BaseEntity;')
])
replace_in_file(os.path.join(base_dir, 'user', 'repository', 'UserRepository.java'), [
    ('package org.nashinnov8.multitrack.repository;', 'package org.nashinnov8.multitrack.user.repository;'),
    ('import org.nashinnov8.multitrack.domain.User;', 'import org.nashinnov8.multitrack.user.domain.User;')
])

# 3. tracking
for f in ['Track.java', 'Milestone.java', 'ActivityLog.java']:
    replace_in_file(os.path.join(base_dir, 'tracking', 'domain', f), [
        ('package org.nashinnov8.multitrack.domain;', 'package org.nashinnov8.multitrack.tracking.domain;\n\nimport org.nashinnov8.multitrack.common.domain.BaseEntity;')
    ])
replace_in_file(os.path.join(base_dir, 'tracking', 'domain', 'TrackStatus.java'), [
    ('package org.nashinnov8.multitrack.domain;', 'package org.nashinnov8.multitrack.tracking.domain;')
])
replace_in_file(os.path.join(base_dir, 'tracking', 'domain', 'Track.java'), [
    ('import org.nashinnov8.multitrack.common.domain.BaseEntity;', 'import org.nashinnov8.multitrack.common.domain.BaseEntity;\nimport org.nashinnov8.multitrack.user.domain.User;')
])

for f in ['TrackRepository.java', 'MilestoneRepository.java', 'ActivityLogRepository.java']:
    entity = f.replace('Repository.java', '')
    replace_in_file(os.path.join(base_dir, 'tracking', 'repository', f), [
        ('package org.nashinnov8.multitrack.repository;', 'package org.nashinnov8.multitrack.tracking.repository;'),
        (f'import org.nashinnov8.multitrack.domain.{entity};', f'import org.nashinnov8.multitrack.tracking.domain.{entity};')
    ])

# 4. gamification
for f in ['Badge.java', 'UserBadge.java']:
    replace_in_file(os.path.join(base_dir, 'gamification', 'domain', f), [
        ('package org.nashinnov8.multitrack.domain;', 'package org.nashinnov8.multitrack.gamification.domain;\n\nimport org.nashinnov8.multitrack.common.domain.BaseEntity;')
    ])
replace_in_file(os.path.join(base_dir, 'gamification', 'domain', 'UserBadge.java'), [
    ('import org.nashinnov8.multitrack.common.domain.BaseEntity;', 'import org.nashinnov8.multitrack.common.domain.BaseEntity;\nimport org.nashinnov8.multitrack.user.domain.User;')
])

for f in ['BadgeRepository.java', 'UserBadgeRepository.java']:
    entity = f.replace('Repository.java', '')
    replace_in_file(os.path.join(base_dir, 'gamification', 'repository', f), [
        ('package org.nashinnov8.multitrack.repository;', 'package org.nashinnov8.multitrack.gamification.repository;'),
        (f'import org.nashinnov8.multitrack.domain.{entity};', f'import org.nashinnov8.multitrack.gamification.domain.{entity};')
    ])

print("Refactoring complete.")
