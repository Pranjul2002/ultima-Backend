package com.edutech.edutechbackend.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "user")
@EqualsAndHashCode(exclude = "user")
public class UserSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean emailReminders;
    private boolean testAlerts;
    private boolean progressReports;
    private boolean newCourses;
    private boolean badges;

    private boolean publicProfile;
    private boolean showProgress;

    private boolean twoFactor;
    private boolean loginAlerts;

    private String language;
    private String timezone;
    private Integer dailyGoal;
    private String recoveryEmail;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
}