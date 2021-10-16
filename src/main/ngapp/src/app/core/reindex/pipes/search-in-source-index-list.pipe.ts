import { Pipe, PipeTransform } from '@angular/core';
import {ISourceIndexList} from '../models/project.model';

@Pipe({
  name: 'searchInSourceIndexList'
})
export class SearchInSourceIndexListPipe implements PipeTransform {

  transform(list: ISourceIndexList[], searchTerm: string): any {
    return null;
  }

}
