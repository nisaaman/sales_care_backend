package com.newgen.ntlsnc.globalsettings.entity;

import com.newgen.ntlsnc.common.SuperEntity;
import com.newgen.ntlsnc.common.enums.VehicleOwnership;
import com.newgen.ntlsnc.common.enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * @author nisa
 * @date 4/13/22
 * @time 12:10 PM
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Vehicle extends SuperEntity<Long> {

   @Column(nullable = false, length = 100)
   private String registrationNo;

   @Column(nullable = false, length = 20)
   @Enumerated(EnumType.STRING)
   private VehicleType vehicleType;

   @Column(length = 20)
   @Enumerated(EnumType.STRING)
   private VehicleOwnership vehicleOwnership;

   @Column(nullable = false)
   private Float vehicleHeight = 0.0f;
   @Column(nullable = false)
   private Float vehicleWidth = 0.0f;
   @Column(nullable = false)
   private Float vehicleDepth = 0.0f;
}
