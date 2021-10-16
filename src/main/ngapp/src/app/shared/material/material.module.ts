import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {
  MatTableModule,
  MatFormFieldModule,
  MatProgressBarModule,
  MatDialogModule,
  MatButtonModule,
  MatCheckboxModule,
  MatIconModule,
  MatMenuModule,
  MatInputModule,
  MatTooltipModule,
  MatRadioModule,
  MatSelectModule,
  MatProgressSpinnerModule, MatSortModule
} from '@angular/material';

@NgModule({
  imports: [
    CommonModule,
    MatTableModule,
    MatFormFieldModule,
    MatProgressBarModule,
    MatDialogModule,
    MatButtonModule,
    MatCheckboxModule,
    MatIconModule,
    MatMenuModule,
    MatInputModule,
    MatTooltipModule,
    MatRadioModule,
    MatSelectModule,
    MatProgressSpinnerModule,
    MatSortModule

  ],
  exports: [
    MatTableModule,
    MatFormFieldModule,
    MatProgressBarModule,
    MatDialogModule,
    MatButtonModule,
    MatCheckboxModule,
    MatIconModule,
    MatMenuModule,
    MatInputModule,
    MatTooltipModule,
    MatRadioModule,
    MatSelectModule,
    MatProgressSpinnerModule,
    MatSortModule
  ]

})
export class MaterialModule {
}

