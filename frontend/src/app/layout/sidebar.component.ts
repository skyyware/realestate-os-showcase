import { Component, EventEmitter, Input, Output } from '@angular/core';

export type SidebarSection = 'overview' | 'properties' | 'units' | 'finances' | 'tasks' | 'documents' | 'decisions' | 'activity' | 'communication' | 'settings';

interface NavigationItem {
  section: SidebarSection;
  label: string;
  icon: string;
}

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss'
})
export class SidebarComponent {
  @Input({ required: true }) activeSection!: SidebarSection;

  @Output() sectionSelected = new EventEmitter<SidebarSection>();
  @Output() loggedOut = new EventEmitter<void>();

  protected readonly navigation: NavigationItem[] = [
    { section: 'overview', label: 'Übersicht', icon: 'home' },
    { section: 'properties', label: 'Immobilien', icon: 'building' },
    { section: 'units', label: 'Einheiten', icon: 'grid' },
    { section: 'finances', label: 'Finanzen', icon: 'finance' },
    { section: 'tasks', label: 'Aufgaben', icon: 'task' },
    { section: 'documents', label: 'Dokumente', icon: 'document' },
    { section: 'activity', label: 'Aktivität', icon: 'activity' },
    { section: 'decisions', label: 'Beschlüsse', icon: 'decision' },
    { section: 'communication', label: 'Kommunikation', icon: 'message' },
    { section: 'settings', label: 'Einstellungen', icon: 'settings' }
  ];

  protected selectSection(section: SidebarSection): void {
    this.sectionSelected.emit(section);
  }

  protected logout(): void {
    this.loggedOut.emit();
  }
}
