import {AfterViewInit, Directive, ElementRef, Input} from '@angular/core';

@Directive({
  selector: '[ylCustomSpinner]'
})
export class CustomSpinnerDirective implements AfterViewInit {
  @Input() color: string;

  constructor(private elem: ElementRef) {
  }

  ngAfterViewInit(): void {
    const element = this.elem.nativeElement;
    const circle = element.querySelector('circle');
    circle.style.stroke = this.color;
  }
}
